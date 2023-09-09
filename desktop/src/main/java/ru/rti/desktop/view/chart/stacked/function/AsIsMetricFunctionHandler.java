package ru.rti.desktop.view.chart.stacked.function;

import org.fbase.core.FStore;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.view.chart.StackedChart;
import ru.rti.desktop.view.chart.stacked.MetricFunctionDataHandler;

import java.util.Set;

public class AsIsMetricFunctionHandler extends MetricFunctionDataHandler {
    public AsIsMetricFunctionHandler(Metric metric, QueryInfo queryInfo, FStore fStore) {
        super(metric, queryInfo, fStore);
    }

    @Override
    public void fillSeriesDataForHistory(ChartInfo chartInfo, Set<String> series) {

    }

    @Override
    public void handleFunction(ChartInfo chartInfo, long beginRange, long endRange, boolean isClientRealTime,
        long finalX, double yK, Set<String> series, StackedChart stackedChart) {

    }

}
