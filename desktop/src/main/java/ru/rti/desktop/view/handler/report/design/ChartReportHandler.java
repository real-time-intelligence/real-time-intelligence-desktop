package ru.rti.desktop.view.handler.report.design;

import static ru.rti.desktop.helper.ProgressBarHelper.createProgressBar;
import static ru.rti.desktop.model.SourceConfig.COLUMNS;
import static ru.rti.desktop.model.SourceConfig.METRICS;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JSplitPane;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.model.profile.CProfile;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.SourceConfig;
import ru.rti.desktop.model.chart.CategoryTableXYDatasetRealTime;
import ru.rti.desktop.model.config.Metric;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.chart.HelperChart;
import ru.rti.desktop.view.chart.report.ClientHistorySCRP;
import ru.rti.desktop.view.chart.report.ServerClientHistoryOneSCRP;
import ru.rti.desktop.view.chart.report.ServerHistorySCRP;
import ru.rti.desktop.view.chart.report.StackChartReportPanel;
import ru.rti.desktop.view.detail.GanttReportDataPanel;
import ru.rti.desktop.view.panel.report.JXTaskPaneForChartCard;

@Log4j2
public abstract class ChartReportHandler implements HelperChart {

    protected ExecutorService executorService;
    protected final ProfileManager profileManager;
    protected final EventListener eventListener;
    protected final FStore fStore;


    public ChartReportHandler(ProfileManager profileManager,
                              EventListener eventListener,
                              FStore fStore
    ) {

        this.profileManager = profileManager;
        this.eventListener = eventListener;
        this.fStore = fStore;
        this.executorService = Executors.newSingleThreadExecutor();
    }


    protected void loadChart(int cId, ChartInfo chartInfo, ProfileTaskQueryKey key,
                             JXTaskPaneForChartCard cardChart, SourceConfig sourceConfig) {

        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }

        this.checkTimestampColumn(tableInfo);

        if (METRICS.equals(sourceConfig)) {
            this.loadChartMetric(cId, chartInfo, key, cardChart);
        } else if (COLUMNS.equals(sourceConfig)) {
            this.loadChartColumn(cId, chartInfo, key, cardChart);
        }

        log.info("Query: " + key.getQueryId());
    }

    private void loadChartMetric(int metricId, ChartInfo chartInfo, ProfileTaskQueryKey key,
                                 JXTaskPaneForChartCard cardChart) {
        log.info("Metric ID: " + metricId);
        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }
        Metric metric = queryInfo.getMetricList()
                .stream().filter(f -> f.getId() == metricId)
                .findAny()
                .orElseThrow(() -> new NotFoundException("Not found metric by id: " + metricId));

        executorService.submit(() -> {
            try {
                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(),
                        createProgressBar("Loading, please wait..."), JSplitPane.TOP, 200);

                if (metric.isStackedYAxisSameCount()) {
                    StackChartReportPanel stackChartPanel = getStackChartPanel(metric.getYAxis(), queryInfo, key, chartInfo);
                    GanttReportDataPanel ganttDataPanel = new GanttReportDataPanel(fStore, queryInfo, tableInfo,
                            metric.getYAxis(), stackChartPanel.getSeriesColorMap());
                    stackChartPanel.setMaximumSize(new Dimension(680, 200));
                    stackChartPanel.setMinimumSize(new Dimension(680, 200));
                    stackChartPanel.setPreferredSize(new Dimension(680, 200));

                    ganttDataPanel.setMaximumSize(new Dimension(680, 200));
                    ganttDataPanel.setMinimumSize(new Dimension(680, 200));
                    ganttDataPanel.setPreferredSize(new Dimension(680, 200));


                    stackChartPanel.addChartListenerReleaseMouse(ganttDataPanel);

                    GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), stackChartPanel, JSplitPane.TOP, 200);
                    GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), ganttDataPanel, JSplitPane.BOTTOM, 200);
                }


                if (metric.isLinearYAxisSameSum()) {
                    StackChartReportPanel stackChartPanel = getOneStackChartPanel(metric.getYAxis(), queryInfo, key, chartInfo);
                    GanttReportDataPanel ganttDataPanel = new GanttReportDataPanel(fStore, queryInfo, tableInfo,
                            metric.getYAxis(), stackChartPanel.getSeriesColorMap());

                    stackChartPanel.addChartListenerReleaseMouse(ganttDataPanel);

                    GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), stackChartPanel, JSplitPane.TOP, 200);
                    GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), ganttDataPanel, JSplitPane.BOTTOM, 200);

                }
                cardChart.setCollapsed(true);
                cardChart.setCollapsed(false);

                cardChart.getJSplitPane().setResizeWeight(0);
                cardChart.getJSplitPane().setResizeWeight(1);
                cardChart.repaint();
                cardChart.revalidate();


            } catch (Exception exception) {
                log.catching(exception);
                throw new RuntimeException(exception);
            }
        });

        log.info("Metric id: " + metricId);
    }

    private void loadChartColumn(int cProfileId, ChartInfo chartInfo, ProfileTaskQueryKey key, JXTaskPaneForChartCard cardChart) {

        QueryInfo queryInfo = profileManager.getQueryInfoById(key.getQueryId());
        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }

        executorService.submit(() -> {
            try {

                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(),
                        createProgressBar("Loading, please wait..."), JSplitPane.TOP, 200);

                CProfile cProfile = tableInfo.getCProfiles()
                        .stream()
                        .filter(f -> f.getColId() == cProfileId)
                        .findAny()
                        .orElseThrow(() -> new NotFoundException("Not found CProfile: " + cProfileId));

                StackChartReportPanel stackChartPanel = getStackChartPanel(cProfile, queryInfo, key, chartInfo);
                GanttReportDataPanel ganttDataPanel = new GanttReportDataPanel(fStore, queryInfo, tableInfo,
                        cProfile, stackChartPanel.getSeriesColorMap());

                stackChartPanel.setMaximumSize(new Dimension(680, 200));
                stackChartPanel.setMinimumSize(new Dimension(680, 200));
                stackChartPanel.setPreferredSize(new Dimension(680, 200));

                ganttDataPanel.setMaximumSize(new Dimension(680, 200));
                ganttDataPanel.setMinimumSize(new Dimension(680, 200));
                ganttDataPanel.setPreferredSize(new Dimension(680, 200));


                stackChartPanel.addChartListenerReleaseMouse(ganttDataPanel);

                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), stackChartPanel, JSplitPane.TOP, 200);
                GUIHelper.addToJSplitPane(cardChart.getJSplitPane(), ganttDataPanel, JSplitPane.BOTTOM, 200);

                cardChart.setCollapsed(true);
                cardChart.setCollapsed(false);

                cardChart.getJSplitPane().setResizeWeight(0);
                cardChart.getJSplitPane().setResizeWeight(1);

                cardChart.setScrollOnExpand(true);
                cardChart.repaint();
                cardChart.revalidate();

            } catch (Exception exception) {
                log.catching(exception);
                throw new RuntimeException(exception);
            }
        });

        log.info("Column profile id: " + cProfileId);
    }


    protected StackChartReportPanel getStackChartPanel(CProfile cProfile, QueryInfo queryInfo,
                                                       ProfileTaskQueryKey profileTaskQueryKey, ChartInfo chartInfo) {
        CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

        StackChartReportPanel stackChartReportPanel = null;


        if (GatherDataSql.BY_CLIENT.equals(queryInfo.getGatherDataSql())) {
            stackChartReportPanel =
                    new ClientHistorySCRP(categoryTableXYDatasetRealTime, profileTaskQueryKey,
                            queryInfo, chartInfo, cProfile, fStore);
        } else if (GatherDataSql.BY_SERVER.equals(queryInfo.getGatherDataSql())) {
            stackChartReportPanel =
                    new ServerHistorySCRP(categoryTableXYDatasetRealTime, profileTaskQueryKey,
                            queryInfo, chartInfo, cProfile, fStore);
        }

        assert stackChartReportPanel != null;
        stackChartReportPanel.initialize();

        return stackChartReportPanel;
    }


    protected StackChartReportPanel getOneStackChartPanel(CProfile cProfile, QueryInfo queryInfo,
                                                          ProfileTaskQueryKey profileTaskQueryKey, ChartInfo chartInfo) {
        CategoryTableXYDatasetRealTime categoryTableXYDatasetRealTime = new CategoryTableXYDatasetRealTime();

        StackChartReportPanel oneStackChartPanel = null;

        oneStackChartPanel = new ServerClientHistoryOneSCRP(categoryTableXYDatasetRealTime,
                profileTaskQueryKey, queryInfo, chartInfo, cProfile, fStore);

        assert oneStackChartPanel != null;
        oneStackChartPanel.initialize();

        return oneStackChartPanel;
    }


    private void checkTimestampColumn(TableInfo tableInfo) {
        if (Objects.isNull(tableInfo.getCProfiles())) {
            throw new NotFoundException("Metadata not found, need to reload query metadata in configuration");
        }

        tableInfo.getCProfiles().stream()
                .filter(f -> f.getCsType().isTimeStamp())
                .findAny()
                .orElseThrow(
                        () -> new NotFoundException("Not found column timestamp for table: " + tableInfo.getTableName()));
    }

}
