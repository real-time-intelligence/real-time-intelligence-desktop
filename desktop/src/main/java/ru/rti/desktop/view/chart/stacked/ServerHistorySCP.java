package ru.rti.desktop.view.chart.stacked;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;

@Log4j2
public class ServerHistorySCP extends StackChartPanel {

    public ServerHistorySCP(WorkspaceQueryComponent workspaceQueryComponent,
                            CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime,
                            ProfileTaskQueryKey profileTaskQueryKey,
                            QueryInfo queryInfo,
                            ChartInfo chartInfo,
                            ProcessType processType,
                            Metric metric) {
        super(workspaceQueryComponent,
              categoryTableXYDatasetRealTime,
              profileTaskQueryKey,
              queryInfo,
              chartInfo,
              processType,
              metric);
    }

    public void initialize() {
        this.batchSize = Math.toIntExact(Math.round((double) (getRangeHistory(chartInfo) / 1000) / MAX_POINT_PER_GRAPH));

        initializeHistory();
    }

    protected void loadData() {
        fStore.syncBackendDb();

        dataHandler.fillSeriesDataForHistory(chartInfo, series);

        ChartRange chartRange = getRange(chartInfo);

        double range = (double) getRangeHistory(chartInfo) / MAX_POINT_PER_GRAPH;
        for (long dtBegin = chartRange.getBegin(); dtBegin <= chartRange.getEnd(); dtBegin += Math.round(range)) {

            long dtEnd = dtBegin + Math.round(range) - 1;

            double k = (double) Math.round(range) / 1000;

            dataHandler.handleFunction(chartInfo, dtBegin, dtEnd,false, dtBegin, k, series, stackedChart);
        }
    }

}
