package ru.rti.desktop.view.chart.stacked;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.fbase.exception.BeginEndWrongOrderException;
import org.fbase.exception.SqlColMetadataException;
import org.fbase.model.output.StackedColumn;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.chart.ValuableGapKey;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;

@Log4j2
public class ClientRealTimeSCP extends StackChartPanel {

  private boolean needToFillStackedChartWhenDataFound = false;
  private boolean isRunningPause = false;

  public ClientRealTimeSCP(WorkspaceQueryComponent workspaceQueryComponent,
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
    this.batchSize = Math.toIntExact(Math.round((double) (getRangeRealTime(chartInfo) / 1000) / MAX_POINT_PER_GRAPH));

    this.range = (double) getRangeRealTime(chartInfo) / MAX_POINT_PER_GRAPH;

    if (range / 1000 < chartInfo.getPullTimeout()) {
      this.range = (double)  chartInfo.getPullTimeout() * 1000;
    }

    initializeRealTime();
  }

  @Override
  protected void loadData() {
    log.info(getHourMinSec(queryInfo.getDeltaLocalServerTime()));

    try {
      fillStackedChartBeforeStart();
    } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
      log.error(e);
      throw new RuntimeException(e);
    }
  }

  private void fillStackedChartBeforeStart() throws BeginEndWrongOrderException, SqlColMetadataException {
    long serverDateTime = System.currentTimeMillis() - queryInfo.getDeltaLocalServerTime();
    long beginCurrentRange = serverDateTime - getRangeRealTime(chartInfo);
    long endCurrentRange = serverDateTime;

    if (needToFillStackedChartWhenDataFound) {
      log.info("Fill stacked chart with empty data when we get the first series");
      List<StackedColumn> sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), begin, end);
      sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
      fillSeries(sColumnList);

      if (series.isEmpty()) {
        fillWithEmptyStackedChart(beginCurrentRange, end);
      } else {
        fillWithEmptyStackedChart(beginCurrentRange, begin - 1);
      }

      needToFillStackedChartWhenDataFound = false;
    }

    if (sqlQueryState.getLastTimestamp(profileTaskQueryKey) == 0 | series.isEmpty()) {
      log.info("Handle initial loading");

      List<StackedColumn> sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(),
              metric.getYAxis(), beginCurrentRange, endCurrentRange);
      sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
      fillSeries(sColumnList);

      if (series.isEmpty()) {
        log.info("No data in current range. Try to get last data from history");
        long last = fStore.getLastTimestamp(queryInfo.getName(), Long.MIN_VALUE, Long.MAX_VALUE);

        if (last != 0) {
          log.info("Fill the data from history");

          if (last < beginCurrentRange) {
            long beginFromHistory = last - getRangeRealTime(chartInfo);
            long endFromHistory = last;

            sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(),
                    metric.getYAxis(), beginFromHistory, endFromHistory);
            sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
            fillSeries(sColumnList);

            fillWithEmptyStackedChart(beginCurrentRange, endCurrentRange);
          } else {
            fillStackedChart(last, end);
          }

        } else {
          log.info("Need try to fill data when the first run");
          needToFillStackedChartWhenDataFound = true;
        }
      } else {
        log.info("Fill the data after pause");

        stackedChart.deleteAllSeriesData(0);

        log.info(toLocalDateTimeOfEpochMilli(beginCurrentRange));
        log.info(toLocalDateTimeOfEpochMilli(endCurrentRange));

        fillStackedChartAfterPause(beginCurrentRange, endCurrentRange);
      }
    } else {
      log.info("Handle regular update");

      if (isRunningPause) {
        log.info("Waiting while load data to stacked chart after pause has been done");
        return;
      }

      if ((clientBegin - stackedChart.getEndXValue()) > range * 3L) {
        log.info("Handle gaps after pause");
        if (stackedChart.getEndXValue() < beginCurrentRange) {
          stackedChart.deleteAllSeriesData(0);
          fillWithEmptyStackedChart(beginCurrentRange,  (long) (clientBegin - range));
        }

        if (stackedChart.getEndXValue() > beginCurrentRange) {
          fillWithEmptyStackedChart((long) (stackedChart.getEndXValue() + range), (long) (clientBegin - range));
        }
      }

      log.info(toLocalDateTimeOfEpochMilli(clientBegin));

      if ((end - clientBegin) >= range) {
        fillStackedChart(clientBegin, end - 1);
        clientBegin = end - 1;
        log.info("Fill stacked chart..");
      } else {
        log.info("Pause..");
      }

      stackedChart.deleteAllSeriesData(chartInfo.getRange());
    }
  }

  private void fillStackedChartAfterPause(long beginRange, long endRange) {
    isRunningPause = true;

    Map<ValuableGapKey, List<StackedColumn>> valuableGapKeyMap = new HashMap<>();

    List<StackedColumn> sColumnList;
    try {
      sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), beginRange, endRange);
      sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
      fillSeries(sColumnList);

      if (sColumnList.isEmpty()) {
        fillWithEmptyStackedChart(beginRange, endRange);

        clientBegin = endRange;

        isRunningPause = false;
        return;
      }

      if (sColumnList.get(sColumnList.size() - 1).getKey() < beginRange) {
        log.info("Fill the empty values");
        fillWithEmptyStackedChart(beginRange, endRange);

        isRunningPause = false;
        return;
      }

      for (int i = 0; i < sColumnList.size(); i++) {
        if (i != sColumnList.size() - 1) { // not last row
          StackedColumn scCurrent = sColumnList.get(i);
          StackedColumn scNext = sColumnList.get(i + 1);

          if (scNext.getKey() - scCurrent.getKey() > chartInfo.getPullTimeout() * 2L * 1000) {
            valuableGapKeyMap.put(new ValuableGapKey(scCurrent.getKey(), scNext.getKey()), Collections.emptyList());
          }

        }// not last row
      }

      if ((sColumnList.get(0).getKey() - beginRange) > range * 2L) {
        log.info("Fill the empty values");
        fillWithEmptyStackedChart(beginRange, (long) (sColumnList.get(0).getKey() - range));
      }

      if (!sColumnList.isEmpty()) {
        for (long dtBegin = beginRange; dtBegin <= endRange; dtBegin += Math.round(range)) {

          long dtEnd = dtBegin + Math.round(range) - 1;
          List<StackedColumn> sColumnListLocal;
          try {
            sColumnListLocal = fStore.getSColumnListByCProfile(queryInfo.getName(),  metric.getYAxis(), dtBegin, dtEnd);
            sColumnListLocal.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
            fillSeries(sColumnListLocal);
          } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
            isRunningPause = false;
            throw new RuntimeException(e);
          }

          log.info("Global batch size: " + batchSize);
          int batchSizeRange = sColumnListLocal.size();

          if (batchSizeRange < ((double) batchSize / chartInfo.getPullTimeout())) {
            log.info("Batch size of current range is lover than common one");
            long prevBegin = dtBegin - Math.round(range);
            long prevEnd = dtBegin - 1;
            log.info(toLocalDateTimeOfEpochMilli(prevBegin));
            log.info(toLocalDateTimeOfEpochMilli(prevEnd));

            int prevBatchSize = getBatchSize(prevBegin, prevEnd);

            if (dtBegin != endRange) {
              long nextBegin = dtEnd + 1;
              long nextEnd = dtEnd + Math.round(range);
              log.info(toLocalDateTimeOfEpochMilli(nextBegin));
              log.info(toLocalDateTimeOfEpochMilli(nextEnd));

              int nextBatchSize = getBatchSize(nextBegin, nextEnd);

              log.info("Current batch size: " + batchSizeRange + " with prev and next one: " + prevBatchSize + ":" + nextBatchSize);

              if (prevBatchSize != 0 & nextBatchSize != 0) {
                if (batchSizeRange == 0 & ((batchSize / chartInfo.getPullTimeout()) == prevBatchSize)
                        & ((batchSize / chartInfo.getPullTimeout()) == nextBatchSize)) {
                  continue;
                }

                if (batchSizeRange == 0 & (prevBatchSize == nextBatchSize)) {
                  continue;
                }

                if (((batchSize / chartInfo.getPullTimeout()) == prevBatchSize)
                        & ((batchSize / chartInfo.getPullTimeout()) == nextBatchSize)) {
                  continue;
                }
              }

            }
          }

          log.info("Fill the data with batch: " + batchSizeRange);
          log.info(toLocalDateTimeOfEpochMilli(dtBegin));
          log.info(toLocalDateTimeOfEpochMilli(dtEnd));

          if (batchSizeRange == 0 & dtBegin == endRange) { // last range
            clientBegin = dtBegin;

            isRunningPause = false;
            return;
          } else {
            clientBegin = dtEnd;
          }

          double k;
          if (range / 1000 < chartInfo.getPullTimeout()) {
            k = chartInfo.getPullTimeout();
          } else {
            k = range / 1000;
          }

          dataHandler.handleFunction(chartInfo, dtBegin, dtEnd, false, dtBegin, k, series, stackedChart);
        }
      }

      if ((endRange - stackedChart.getEndXValue()) > range * 3L * 1000) {
        fillWithEmptyStackedChart((long) (stackedChart.getEndXValue() + range), endRange);
      }

      clientBegin = endRange;

    } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
      isRunningPause = false;
      throw new RuntimeException(e);
    }

    isRunningPause = false;
  }

  private void fillStackedChart(long beginRange, long endRange) {
    log.info("Fill stacked chart regular");
    log.info(toLocalDateTimeOfEpochMilli(beginRange));
    log.info(toLocalDateTimeOfEpochMilli(endRange));

    double k;
    if (range / 1000 < chartInfo.getPullTimeout()) {
      k = chartInfo.getPullTimeout();
    } else {
      k = (range / 1000);
    }

    dataHandler.handleFunction(chartInfo, beginRange, endRange,true, clientBegin, k, series, stackedChart);
  }

  private int getBatchSize(long beginRange, long endRange) {
    try {
      List<StackedColumn> sColumnList = fStore.getSColumnListByCProfile(queryInfo.getName(),  metric.getYAxis(), beginRange, endRange);
      sColumnList.removeIf(stackedColumn -> stackedColumn.getKeyCount().isEmpty());
      fillSeries(sColumnList);

      return sColumnList.size();

    } catch (SqlColMetadataException | BeginEndWrongOrderException e) {
      throw new RuntimeException(e);
    }
  }

}
