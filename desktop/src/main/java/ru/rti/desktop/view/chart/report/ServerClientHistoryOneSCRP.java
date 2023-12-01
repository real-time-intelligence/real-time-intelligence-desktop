package ru.rti.desktop.view.chart.report;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.AsIsValueTyped;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.chart.ChartRange;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ServerClientHistoryOneSCRP extends StackChartReportPanel {


    public ServerClientHistoryOneSCRP(CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime,
                                      ProfileTaskQueryKey profileTaskQueryKey,
                                      QueryInfo queryInfo,
                                      ChartInfo chartInfo,
                                      Metric metric,
                                      FStore fStore
    ) {
        super(categoryTableXYDatasetRealTime, profileTaskQueryKey, queryInfo, chartInfo,
                metric, fStore);
    }

    @Override
    public void initialize() {
        this.series.add(metric.getYAxis().getColName());
        this.batchSize = Math.toIntExact(Math.round((double) (getRangeHistory(chartInfo) / 1000) / MAX_POINT_PER_GRAPH));

        initializeHistory();
    }

    @Override
    protected void loadData() {
        fStore.syncBackendDb();

        ChartRange chartRange = getRange(chartInfo);

        double range = (double) getRangeHistory(chartInfo) / MAX_POINT_PER_GRAPH;
        for (long dtBegin = chartRange.getBegin(); dtBegin <= chartRange.getEnd(); dtBegin += Math.round(range)) {
            long dtEnd = dtBegin + Math.round(range) - 1;

            List<List<Object>> rawDataByColumn =
                    fStore.getRawDataByColumn(queryInfo.getName(), metric.getYAxis(), dtBegin, dtEnd);

            try {
                List<AsIsValueTyped> objectsAll = new ArrayList<>();
                for (List<Object> objects : rawDataByColumn) {
                    objectsAll.add(new AsIsValueTyped((Long) objects.get(0), Double.parseDouble((String) objects.get(1))));
                }

                Map<Long, DoubleSummaryStatistics> statisticsHashMap = new HashMap<>();

                objectsAll.stream()
                        .collect(Collectors.groupingBy(AsIsValueTyped::getTimestamp, TreeMap::new, Collectors.toList()))
                        .forEach((k, v) -> statisticsHashMap.put(k, v.stream()
                                .collect(Collectors.summarizingDouble(AsIsValueTyped::getValue))));

                statisticsHashMap.forEach((key, value) ->
                        categoryTableXYDatasetRealTime.add(key.doubleValue(), value.getAverage(),
                                series.stream().findAny().orElseThrow()));

            } catch (Exception exception) {
                log.info(exception);
            }

        }

    }

}
