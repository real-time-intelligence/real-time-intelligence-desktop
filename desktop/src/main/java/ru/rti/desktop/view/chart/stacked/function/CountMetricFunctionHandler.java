package ru.rti.desktop.view.chart.stacked.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.exception.BeginEndWrongOrderException;
import org.fbase.exception.SqlColMetadataException;
import org.fbase.model.output.StackedColumn;
import ru.rti.desktop.exception.SeriesExceedException;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.view.chart.StackedChart;
import ru.rti.desktop.view.chart.stacked.MetricFunctionDataHandler;

@Log4j2
public class CountMetricFunctionHandler extends MetricFunctionDataHandler {
    public CountMetricFunctionHandler(Metric metric, QueryInfo queryInfo, FStore fStore) {
        super(metric, queryInfo, fStore);
    }

    @Override
    public void fillSeriesDataForHistory(ChartInfo chartInfo, Set<String> series) {
        List<StackedColumn> sColumnList;
        try {
            ChartRange chartRange = getRange(chartInfo);

            sColumnList = new ArrayList<>(
                    fStore.getSColumnListByCProfile(queryInfo.getName(),  metric.getYAxis(),
                            chartRange.getBegin(), chartRange.getEnd()));

            sColumnList.stream()
                    .map(StackedColumn::getKeyCount)
                    .map(Map::keySet)
                    .flatMap(Collection::stream)
                    .forEach(series::add);
        } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFunction(ChartInfo chartInfo, long beginRange, long endRange,
                                     boolean isClientRealTime, long finalX, double yK,
                                     Set<String> series, StackedChart stackedChart) {
        try {
            List<StackedColumn> sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), beginRange, endRange);
            sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());

            long x;

            if (isClientRealTime) {
                x = sColumnList.isEmpty() ? finalX : sColumnList.get(0).getKey();
            } else {
                x = finalX;
            }

            checkSeriesCount(series);
            fillSeries(sColumnList, series);
            checkSeriesCount(series);

            Map<String, IntSummaryStatistics> batchData = sColumnList.stream()
                    .toList()
                    .stream()
                    .map(StackedColumn::getKeyCount)
                    .flatMap(sc -> sc.entrySet().stream())
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summarizingInt(Map.Entry::getValue)));

            series.forEach(seriesName -> {
                Optional<IntSummaryStatistics> batch = Optional.ofNullable(batchData.get(seriesName));
                stackedChart.setSeriesPaintDynamic(seriesName);

                try {
                    double y = sColumnList.size() == 0 ? 0D : ((double) batch.map(IntSummaryStatistics::getSum).orElse(0L) / (yK));

                    stackedChart.addSeriesValue(x, y, seriesName);
                } catch (Exception exception) {
                    log.info(exception);
                }
            });

        } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkSeriesCount(Set<String> series) {
        if (series.size() > 50) {
           throw new SeriesExceedException("Column data series exceeds 50. Not supported to show stacked data..");
        }
    }

}
