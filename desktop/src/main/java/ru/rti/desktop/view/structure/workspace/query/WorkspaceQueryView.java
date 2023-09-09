package ru.rti.desktop.view.structure.workspace.query;

import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.table.TableColumn;
import lombok.extern.log4j.Log4j2;
import org.jdesktop.swingx.JXTitledSeparator;
import org.painlessgridbag.PainlessGridBag;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryModule;
import ru.rti.desktop.config.prototype.task.WorkspaceTaskComponent;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.column.MetricsColumnNames;
import ru.rti.desktop.model.column.QueryMetadataColumnNames;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.AppCacheAddListener;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.panel.QuerySearchButtonPanel;
import ru.rti.desktop.view.panel.RangeChartCustomPanel;
import ru.rti.desktop.view.panel.RangeChartHistoryPanel;
import ru.rti.desktop.view.panel.RangeChartRealTimePanel;
import ru.rti.desktop.view.panel.TimeRangeAbsolutePanel;
import ru.rti.desktop.view.panel.TimeRangeRecentPanel;
import ru.rti.desktop.view.panel.TimeRangeRelativePanel;
import ru.rti.desktop.view.structure.workspace.handler.DetailsControlPanelHandler;
import ru.rti.desktop.view.structure.workspace.handler.MetricColumnSelectionHandler;
import ru.rti.desktop.view.structure.workspace.handler.QuerySearchHandler;
import ru.rti.desktop.view.structure.workspace.handler.TimeRangeAbsoluteHandler;
import ru.rti.desktop.view.structure.workspace.handler.TimeRangeQuickHandler;
import ru.rti.desktop.view.structure.workspace.handler.TimeRangeRecentHandler;
import ru.rti.desktop.view.structure.workspace.handler.TimeRangeRelativeHandler;
import ru.rti.desktop.view.structure.workspace.task.QueryMetricColumnContainer;

@Log4j2
public class WorkspaceQueryView extends JPanel {

    private final ProfileTaskQueryKey profileTaskQueryKey;
    private final JSplitPane chartGanttPanelRealTime;
    private final JSplitPane chartGanttPanelHistory;
    private final JSplitPane chartPanelSearch;

    private final QueryMetricColumnContainer listCardQueryMetricColumn;

    private final JXTableCase jxTableCaseMetrics;
    private final JXTableCase jxTableCaseColumns;
    private final JXTableCase jxTableCaseRecent;

    private final DetailsControlPanel detailsControlPanel;
    private final DetailsControlPanelHandler detailsControlPanelHandler;

    private final WorkspaceTaskComponent workspaceTaskComponent;
    private final WorkspaceQueryComponent workspaceQueryComponent;

    private final RangeChartRealTimePanel rangeChartRealTimePanel;
    private final RangeChartHistoryPanel rangeChartHistoryPanel;
    private final RangeChartCustomPanel rangeChartCustomPanel;
    private final TimeRangeRelativePanel timeRangeRelativePanel;
    private final TimeRangeAbsolutePanel timeRangeAbsolutePanel;
    private final TimeRangeRecentPanel timeRangeRecentPanel;
    private final QuerySearchButtonPanel querySearchButtonPanel;

    private final MetricColumnSelectionHandler metricColumnSelectionHandler;
    private final TimeRangeQuickHandler timeRangeQuickHandler;
    private final TimeRangeRelativeHandler timeRangeRelativeHandler;
    private final TimeRangeAbsoluteHandler timeRangeAbsoluteHandler;
    private final TimeRangeRecentHandler timeRangeRecentHandler;
    private final QuerySearchHandler querySearchHandler;
    private final CustomHistoryPanel customHistoryPanel;
    private final JTabbedPane jTabbedPane;

    @Inject
    @Named("eventListener")
    EventListener eventListener;

    @Inject
    @Named("profileManager")
    ProfileManager profileManager;

    public WorkspaceQueryView(ProfileTaskQueryKey profileTaskQueryKey,
                              JSplitPane chartGanttPanelRealTime,
                              JSplitPane chartGanttPanelHistory,
                              JSplitPane chartPanelSearch,
                              ChartJTabbedPane chartJTabbedPane,
                              QueryMetricColumnContainer listCardQueryMetricColumn,
                              WorkspaceTaskComponent workspaceTaskComponent) {
        this.workspaceTaskComponent = workspaceTaskComponent;
        this.workspaceQueryComponent = this.workspaceTaskComponent.initQuery(new WorkspaceQueryModule(this));
        this.workspaceQueryComponent.inject(this);

        this.listCardQueryMetricColumn = listCardQueryMetricColumn;

        this.profileTaskQueryKey = profileTaskQueryKey;

        this.chartGanttPanelRealTime = chartGanttPanelRealTime;
        this.chartGanttPanelHistory = chartGanttPanelHistory;
        this.chartPanelSearch = chartPanelSearch;

        this.jxTableCaseMetrics = getJXTableCaseMetrics();
        this.jxTableCaseColumns = getJXTableCaseColumns();
        this.jxTableCaseRecent = getJXTableCaseRecent();

        this.detailsControlPanel = new DetailsControlPanel();
        this.detailsControlPanelHandler = new DetailsControlPanelHandler(this.detailsControlPanel);

        this.rangeChartRealTimePanel = new RangeChartRealTimePanel();
        this.rangeChartRealTimePanel.setEnabled(true);
        this.rangeChartHistoryPanel = new RangeChartHistoryPanel();
        this.rangeChartHistoryPanel.setEnabled(true);
        this.rangeChartCustomPanel = new RangeChartCustomPanel();
        this.rangeChartCustomPanel.setEnabled(true);
        this.timeRangeRelativePanel = new TimeRangeRelativePanel();
        this.timeRangeRelativePanel.setEnabled(true);
        this.timeRangeAbsolutePanel = new TimeRangeAbsolutePanel();
        this.timeRangeAbsolutePanel.setEnabled(true);
        this.timeRangeRecentPanel = new TimeRangeRecentPanel(this.jxTableCaseRecent);
        this.timeRangeRecentPanel.setEnabled(true);
        this.querySearchButtonPanel = new QuerySearchButtonPanel();
        this.querySearchButtonPanel.setEnabled(true);

        this.listCardQueryMetricColumn.addMetricToCard(this.jxTableCaseMetrics);
        this.listCardQueryMetricColumn.addColumnToCard(this.jxTableCaseColumns);

        this.customHistoryPanel = new CustomHistoryPanel();
        this.jTabbedPane = new JTabbedPane();

        ProfileInfo profileInfo = profileManager.getProfileInfoById(profileTaskQueryKey.getProfileId());

        QueryInfo queryInfo = profileManager.getQueryInfoById(profileTaskQueryKey.getQueryId());
        if (Objects.isNull(queryInfo)) {
            throw new NotFoundException(String.format("Query info with id=%s not found",
                profileTaskQueryKey.getQueryId()));
        }

        if (RunStatus.NOT_RUNNING.equals(profileInfo.getStatus()) & queryInfo.getDeltaLocalServerTime() == 0) {
            profileManager.loadDeltaLocalServerTime(profileTaskQueryKey);
            queryInfo = profileManager.getQueryInfoById(profileTaskQueryKey.getQueryId());
        }

        TableInfo tableInfo = profileManager.getTableInfoByTableName(queryInfo.getName());
        if (Objects.isNull(tableInfo)) {
            throw new NotFoundException(String.format("Table info with id=%s not found",
                    queryInfo.getName()));
        }

        ChartInfo chartInfo = profileManager.getChartInfoById(queryInfo.getId());
        if (Objects.isNull(chartInfo)) {
            throw new NotFoundException(String.format("Chart info with id=%s not found",
                queryInfo.getId()));
        }

        chartInfo.setRange(10);
        profileManager.updateChart(chartInfo);

        this.metricColumnSelectionHandler = new MetricColumnSelectionHandler(chartJTabbedPane,
            profileTaskQueryKey, queryInfo, tableInfo, chartInfo,
            jxTableCaseMetrics, jxTableCaseColumns,
            chartGanttPanelRealTime, chartGanttPanelHistory, listCardQueryMetricColumn,
            detailsControlPanel, detailsControlPanelHandler, workspaceTaskComponent, workspaceQueryComponent);

        this.timeRangeQuickHandler = new TimeRangeQuickHandler(jxTableCaseMetrics, jxTableCaseColumns,
                        rangeChartRealTimePanel, rangeChartHistoryPanel,
                        chartGanttPanelRealTime, chartGanttPanelHistory, chartJTabbedPane, customHistoryPanel,
                        profileTaskQueryKey, queryInfo, tableInfo, chartInfo, workspaceQueryComponent);

        this.timeRangeRelativeHandler = new TimeRangeRelativeHandler(jxTableCaseMetrics, jxTableCaseColumns,
                timeRangeRelativePanel, chartJTabbedPane, chartGanttPanelRealTime, chartGanttPanelHistory,
                profileTaskQueryKey, queryInfo, tableInfo, chartInfo, workspaceQueryComponent);

        this.timeRangeAbsoluteHandler = new TimeRangeAbsoluteHandler(jxTableCaseMetrics, jxTableCaseColumns,
            timeRangeAbsolutePanel, chartJTabbedPane, profileTaskQueryKey,
            chartGanttPanelRealTime, chartGanttPanelHistory, queryInfo, tableInfo,
            chartInfo, workspaceQueryComponent);

        this.eventListener.clearListener(AppCacheAddListener.class);
        this.timeRangeRecentHandler = new TimeRangeRecentHandler(jxTableCaseMetrics, jxTableCaseColumns, jxTableCaseRecent,
            profileTaskQueryKey, chartJTabbedPane, chartGanttPanelRealTime, chartGanttPanelHistory,
            chartInfo, queryInfo, tableInfo, workspaceQueryComponent);
        this.eventListener.addAppCacheAddListener(this.timeRangeRecentHandler);

        this.querySearchHandler = new QuerySearchHandler(chartPanelSearch, querySearchButtonPanel,
                profileTaskQueryKey, queryInfo, tableInfo, chartInfo, workspaceQueryComponent);

        this.loadColumns(queryInfo, tableInfo);

        this.workspaceQueryComponent.injectMetricColumnSelectionHandler(this.metricColumnSelectionHandler);
        this.workspaceQueryComponent.injectTimeRangeQuickHandler(this.timeRangeQuickHandler);
        this.workspaceQueryComponent.injectTimeRangeRelativeHandler(this.timeRangeRelativeHandler);
        this.workspaceQueryComponent.injectTimeRangeAbsoluteHandler(this.timeRangeAbsoluteHandler);
        this.workspaceQueryComponent.injectTimeRangeRecentHandler(this.timeRangeRecentHandler);
        this.workspaceQueryComponent.injectSearchHandler(this.querySearchHandler);

        this.fillCustomHistoryPanel();

        JPanel panelEntities = new JPanel();
        PainlessGridBag gbl = new PainlessGridBag(panelEntities, GUIHelper.getPainlessGridbagConfigurationNull(),false);

        gbl.row()
            .cellX(new JXTitledSeparator("Metrics"), 2).fillX(4)
            .cellX(new JXTitledSeparator("Columns"), 2).fillX(4)
            .cellX(new JXTitledSeparator("Details"), 2).fillX(4)
            .cellX(new JXTitledSeparator("Real-time"), 1).fillX(1)
            .cellX(new JXTitledSeparator("History"), 1).fillX(1);

        gbl.row()
            .cellX(listCardQueryMetricColumn.getJXTableCaseMetric().getJScrollPane(), 2).fillXY(4,3)
            .cellX(listCardQueryMetricColumn.getJXTableCaseColumn().getJScrollPane(), 2).fillXY(4,3)
            .cellX(detailsControlPanel, 2).fillXY(4,3)
            .cellX(rangeChartRealTimePanel, 1).fillXY(1,1)
            .cellX(rangeChartHistoryPanel, 1).fillXY(1,1);

        gbl.done();

        PainlessGridBag gblThis = new PainlessGridBag(this, GUIHelper.getPainlessGridbagConfiguration(), false);
        gblThis.row().cellXRemainder(panelEntities).fillXY();

        gblThis.done();

        this.eventListener.fireOnAddToAppCache(profileTaskQueryKey);
    }

    private void fillCustomHistoryPanel() {
        jTabbedPane.add("Relative", timeRangeRelativePanel);
        jTabbedPane.add("Absolute", timeRangeAbsolutePanel);
        jTabbedPane.add("Recent", timeRangeRecentPanel);
        jTabbedPane.setMnemonicAt(0, KeyEvent.VK_R);
        jTabbedPane.setMnemonicAt(1, KeyEvent.VK_A);
        jTabbedPane.setMnemonicAt(2, KeyEvent.VK_E);

        customHistoryPanel.add(jTabbedPane);
    }

    private JXTableCase getJXTableCaseMetrics() {
        JXTableCase jxTableCase =
            GUIHelper.getJXTableCase(3,
                new String[] {
                    MetricsColumnNames.ID.getColName(),
                    MetricsColumnNames.NAME.getColName()
            });
        jxTableCase.getJxTable().setSortable(false);

        return jxTableCase;
    }

    private JXTableCase getJXTableCaseColumns() {
        JXTableCase jxTableCase =
            GUIHelper.getJXTableCase(7,
                new String[] {
                    QueryMetadataColumnNames.ID.getColName(),
                    QueryMetadataColumnNames.NAME.getColName()
                });
        jxTableCase.getJxTable().setSortable(false);

        return jxTableCase;
    }

    private JXTableCase getJXTableCaseRecent() {
        JXTableCase jxTableCase = GUIHelper.getJXTableCase(4, new String[]{"Created at", "S", "Begin", "End"});
        jxTableCase.getJxTable().getColumnExt(0).setVisible(false);

        TableColumn col = jxTableCase.getJxTable().getColumnModel().getColumn(0);
        col.setMinWidth(10);
        col.setMaxWidth(15);

        return jxTableCase;
    }

    private void loadColumns(QueryInfo queryInfo, TableInfo tableInfo) {
        jxTableCaseMetrics.getJxTable().getColumnExt(0).setVisible(false);
        jxTableCaseColumns.getJxTable().getColumnExt(0).setVisible(false);

        jxTableCaseMetrics.getJxTable().getColumnModel().getColumn(0)
            .setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());
        jxTableCaseColumns.getJxTable().getColumnModel().getColumn(0)
            .setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());

        if (queryInfo.getMetricList() != null && !queryInfo.getMetricList().isEmpty()) {
            queryInfo.getMetricList()
                .forEach(metric -> jxTableCaseMetrics.getDefaultTableModel()
                    .addRow(new Object[]{metric.getId(), metric.getName()}));
        } else {
            log.warn("No metrics found for " + queryInfo);
        }

        if (tableInfo.getCProfiles() != null) {
            tableInfo.getCProfiles().stream()
                .filter(f -> !f.getCsType().isTimeStamp())
                .forEach(cProfile -> jxTableCaseColumns.getDefaultTableModel()
                    .addRow(new Object[]{cProfile.getColId(), cProfile.getColName()}));
        } else {
            log.warn("Not columns found for " + queryInfo);
        }

        jxTableCaseMetrics.getJxTable().packAll();
        jxTableCaseColumns.getJxTable().packAll();
    }

}
