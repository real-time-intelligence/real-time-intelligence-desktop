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
import ru.rti.desktop.model.config.Metric;
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
                             Metric metric,
                             FStore fStore) {
        super(categoryTableXYDatasetRealTime, profileTaskQueryKey, queryInfo, chartInfo, metric, fStore);
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
