package ru.rti.desktop.view.chart.stacked;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.AsIsValueTyped;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;

@Log4j2
public class ServerClientRealTimeOneSCP extends StackChartPanel {

  public ServerClientRealTimeOneSCP(WorkspaceQueryComponent workspaceQueryComponent,
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

  @Override
  public void initialize() {
    this.series.add(metric.getYAxis().getColName());
    this.stackedChart.setSeriesPaintDynamic(metric.getYAxis().getColName());
    this.batchSize = Math.toIntExact(Math.round((double) (getRangeRealTime(chartInfo) / 1000) / MAX_POINT_PER_GRAPH));

    initializeRealTime();
  }

  @Override
  protected void loadData() {
    long serverDateTime = System.currentTimeMillis() - queryInfo.getDeltaLocalServerTime();

    long begin = serverDateTime - getRangeRealTime(chartInfo);
    long end = serverDateTime;

    if (clientBegin != 0) {
      begin = clientBegin;
      end = System.currentTimeMillis() - queryInfo.getDeltaLocalServerTime();
    }

    List<List<Object>> rawDataByColumn =
        fStore.getRawDataByColumn(queryInfo.getName(), metric.getYAxis(), begin, end);

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

      statisticsHashMap.forEach((key, value) -> categoryTableXYDatasetRealTime.add(key.doubleValue(), value.getAverage(),
          series.stream().findAny().orElseThrow()));

    } catch (Exception exception) {
      log.info(exception);
    }

    if (rawDataByColumn.size() != 0) {
      clientBegin = (long) rawDataByColumn.get(rawDataByColumn.size() - 1).get(0);
    } else {
      clientBegin = end;
    }

    clientBegin = clientBegin + 1;

    stackedChart.deleteAllSeriesData(chartInfo.getRange());
  }

  @Override
  public void fireOnStopCollect(ProfileTaskQueryKey profileTaskQueryKey) {
    log.info("fireOnStartCollect for profileTaskQueryKey:" + profileTaskQueryKey);
    try {
      this.loadData();
    } catch (Exception e) {
      log.catching(e);
    }
  }
}
