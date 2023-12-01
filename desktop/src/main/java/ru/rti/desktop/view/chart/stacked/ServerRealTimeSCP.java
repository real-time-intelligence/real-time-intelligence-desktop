package ru.rti.desktop.view.chart.stacked;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.fbase.exception.BeginEndWrongOrderException;
import org.fbase.exception.SqlColMetadataException;
import org.fbase.model.output.StackedColumn;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.ProcessType;

@Log4j2
public class ServerRealTimeSCP extends StackChartPanel {

    private boolean needToFillStackedChartWhenDataFound = false;

    public ServerRealTimeSCP(WorkspaceQueryComponent workspaceQueryComponent,
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
            List<StackedColumn> sColumnList =
                fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), begin, end);
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

            List<StackedColumn> sColumnList =
                fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), beginCurrentRange, endCurrentRange);
            fillSeries(sColumnList);

            if (series.isEmpty()) {
                log.info("No data in current range. Try to get last data from history");
                long last = fStore.getLastTimestamp(queryInfo.getName(), Long.MIN_VALUE, Long.MAX_VALUE);

                if (last != 0) {
                    log.info("Fill the data from history");

                    if (last < beginCurrentRange) {
                        long beginFromHistory = last - getRangeRealTime(chartInfo);
                        long endFromHistory = last;

                        sColumnList =
                            fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), beginFromHistory, endFromHistory);
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

                sColumnList =
                    fStore.getSColumnListByCProfile(queryInfo.getName(), metric.getYAxis(), beginCurrentRange, endCurrentRange);
                fillSeries(sColumnList);

                fillStackedChart(beginCurrentRange, endCurrentRange);
            }
        } else {
            log.info("Handle regular update");

            long endXValue = (long) stackedChart.getEndXValue();

            if (endXValue < (end - getRangeRealTime(chartInfo))) {
                endXValue = end - getRangeRealTime(chartInfo);
            }

            log.info(toLocalDateTimeOfEpochMilli(endXValue));

            if ((begin - endXValue) > (range * 2)) {
                log.info("Handle gaps after pause");
                if (endXValue > beginCurrentRange) {
                    fillWithEmptyStackedChart((endXValue + 1), begin - 1);
                }
            }

            if ((end - endXValue) > range) {
                log.info("Load data when range lower that pull timeout");
                fillStackedChart(endXValue + 1, end);
            }

            stackedChart.deleteAllSeriesData(chartInfo.getRange());
        }
    }

    private void fillStackedChart(long beginRange, long endRange) {
        log.info("Fill stacked chart regular");
        log.info(toLocalDateTimeOfEpochMilli(beginRange));
        log.info(toLocalDateTimeOfEpochMilli(endRange));

        for (long xBegin = beginRange; xBegin <= endRange; xBegin += Math.round(range)) {
            long xEnd;

            if (endRange - (xBegin + Math.round(range)) < range) {
                xEnd = endRange - 1;
            } else {
                xEnd = xBegin + Math.round(range) - 1;
            }

            log.info(toLocalDateTimeOfEpochMilli(xBegin));
            log.info(toLocalDateTimeOfEpochMilli(xEnd));

            if (xBegin > xEnd) {
                return;
            }

            double k = (double) Math.round(range) / 1000;

            dataHandler.handleFunction(chartInfo, xBegin, xEnd,false, xBegin, k, series, stackedChart);

            if (endRange - (xBegin + Math.round(range)) < range) {
                xBegin = endRange;
            }
        }
    }

}
