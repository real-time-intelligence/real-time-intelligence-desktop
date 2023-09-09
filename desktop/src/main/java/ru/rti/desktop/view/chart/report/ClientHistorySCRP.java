package ru.rti.desktop.view.chart.report;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.exception.BeginEndWrongOrderException;
import org.fbase.exception.SqlColMetadataException;
import org.fbase.model.output.StackedColumn;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.exception.SeriesExceedException;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ClientHistorySCRP extends StackChartReportPanel {
    public ClientHistorySCRP(CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime,
                             ProfileTaskQueryKey profileTaskQueryKey,
                             QueryInfo queryInfo,
                             ChartInfo chartInfo,
                             CProfile cProfile,
                             FStore fStore) {
        super(categoryTableXYDatasetRealTime, profileTaskQueryKey, queryInfo, chartInfo, cProfile, fStore);
    }

    public void initialize() {
        this.batchSize = Math.toIntExact(Math.round((double) (getRangeHistory(chartInfo) / 1000) / MAX_POINT_PER_GRAPH));

        initializeHistory();
    }

    protected void loadData() {
        fStore.syncBackendDb();

        ChartRange chartRange = getRange(chartInfo);

        List<StackedColumn> sColumnList;
        try {
            sColumnList = new ArrayList<>(
                    fStore.getSColumnListByCProfile(queryInfo.getName(), cProfile,
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

        log.info(series);
        if (series.size() > 50) {
            throw new SeriesExceedException("Series more than 50. Not supported to show stacked data..");
        }

        double range = (double) getRangeHistory(chartInfo) / MAX_POINT_PER_GRAPH;
        for (long dtBegin = chartRange.getBegin(); dtBegin <= chartRange.getEnd(); dtBegin += Math.round(range)) {

            long dtEnd = dtBegin + Math.round(range) - 1;

            LocalDateTime beginLocalRange = LocalDateTime.ofInstant(Instant.ofEpochMilli(dtBegin), TimeZone.getDefault().toZoneId());
            LocalDateTime endLocalRange = LocalDateTime.ofInstant(Instant.ofEpochMilli(dtEnd), TimeZone.getDefault().toZoneId());

            List<StackedColumn> sColumnListLocal;
            try {
                sColumnListLocal = fStore.getSColumnListByCProfile(queryInfo.getName(), cProfile, dtBegin, dtEnd);
            } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
                throw new RuntimeException(e);
            }

            sColumnListLocal.stream()
                    .map(StackedColumn::getKeyCount)
                    .map(Map::keySet)
                    .flatMap(Collection::stream)
                    .forEach(series::add);

            batchSize = sColumnListLocal.size();

            Map<String, IntSummaryStatistics> batchDataLocal = sColumnListLocal.stream()
                    .toList()
                    .stream()
                    .map(StackedColumn::getKeyCount)
                    .flatMap(sc -> sc.entrySet().stream())
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summarizingInt(Map.Entry::getValue)));

            long finalDtBegin = dtBegin;
            series.forEach(series -> {
                Optional<IntSummaryStatistics> batch = Optional.ofNullable(batchDataLocal.get(series));
                stackedChart.setSeriesPaintDynamic(series);

                double y;
                try {
                    y = batchSize == 0 ? 0D : ((double) batch.map(IntSummaryStatistics::getSum)
                            .orElse(0L) / batchSize) / chartInfo.getPullTimeout();

                    stackedChart.addSeriesValue(finalDtBegin, y, series);
                } catch (Exception exception) {
                    log.info(exception);
                }
            });
        }
    }

}
