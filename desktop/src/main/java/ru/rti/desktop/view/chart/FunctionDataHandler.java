package ru.rti.desktop.view.chart;

import ru.rti.desktop.model.info.gui.ChartInfo;

import java.util.Set;

public interface FunctionDataHandler extends HelperChart {

    void fillSeriesDataForHistory(ChartInfo chartInfo, Set<String> series);

    void handleFunction(ChartInfo chartInfo, long beginRange, long endRange,
                              boolean isClientRealTime, long finalX, double yK,
                              Set<String> series, StackedChart stackedChart);
}
