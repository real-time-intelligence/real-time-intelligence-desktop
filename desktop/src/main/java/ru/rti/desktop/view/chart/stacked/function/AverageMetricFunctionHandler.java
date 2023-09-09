package ru.rti.desktop.view.chart.stacked.function;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import ru.rti.desktop.model.chart.AsIsValueTyped;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.view.chart.StackedChart;
import ru.rti.desktop.view.chart.stacked.MetricFunctionDataHandler;

import java.util.Set;

@Log4j2
public class AverageMetricFunctionHandler extends MetricFunctionDataHandler {
    public AverageMetricFunctionHandler(Metric metric, QueryInfo queryInfo, FStore fStore) {
        super(metric, queryInfo, fStore);
    }

    @Override
    public void fillSeriesDataForHistory(ChartInfo chartInfo, Set<String> series) {

    }

    @Override
    public void handleFunction(ChartInfo chartInfo, long beginRange, long endRange, boolean isClientRealTime,
        long finalX, double yK, Set<String> series, StackedChart stackedChart) {

        List<List<Object>> rawDataByColumn =
            fStore.getRawDataByColumn(queryInfo.getName(), metric.getYAxis(), beginRange, endRange);

        try {
            List<AsIsValueTyped> objectsAll = new ArrayList<>();
            for (List<Object> objects : rawDataByColumn) {
                objectsAll.add(new AsIsValueTyped((Long) objects.get(0), Double.parseDouble((String) objects.get(1))));
            }

            long x;

            if (isClientRealTime) {
                x = objectsAll.isEmpty() ? finalX : objectsAll.get(0).getTimestamp();
            } else {
                x = finalX;
            }

            OptionalDouble average = objectsAll.stream()
                .mapToDouble(AsIsValueTyped::getValue)
                .average();

            stackedChart.addSeriesValue(x, average.orElse(0D), metric.getYAxis().getColName());

        } catch (Exception exception) {
            log.info(exception);
        }

    }

}
