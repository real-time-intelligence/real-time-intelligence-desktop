package ru.rti.desktop.view.structure.workspace.task;

import java.awt.GridLayout;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.config.prototype.profile.WorkspaceProfileComponent;
import ru.rti.desktop.config.prototype.task.WorkspaceTaskComponent;
import ru.rti.desktop.config.prototype.task.WorkspaceTaskModule;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskKey;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.structure.workspace.handler.QuerySelectionHandler;

@Log4j2
public class WorkspaceTaskView extends JPanel {

  private final JSplitPane sqlPane;
  private final ProfileTaskKey profileTaskKey;
  private final JSplitPane chartByQueryGanttPanel;
  private final JSplitPane chartGanttPanelRealTime;
  private final JSplitPane chartGanttPanelHistory;
  private final JSplitPane chartPanelSearch;

  private final JXTableCase jXTableCaseQuery;

  private final ChartJTabbedPane chartJTabbedPane;

  private final JPanel workspaceQueryMetadataPanel;
  private final QuerySelectionHandler querySelectionHandler;

  private final QueryMetricColumnContainer listCardQueryMetricColumn;

  private final WorkspaceProfileComponent workspaceProfileComponent;
  private final WorkspaceTaskComponent workspaceTaskComponent;

  @Inject
  @Named("eventListener")
  EventListener eventListener;

  @Inject
  @Named("profileManager")
  ProfileManager profileManager;

  public WorkspaceTaskView(JSplitPane sqlPane,
                           ProfileTaskKey profileTaskKey,
                           WorkspaceProfileComponent workspaceProfileComponent) {
    this.workspaceProfileComponent = workspaceProfileComponent;
    this.workspaceTaskComponent = this.workspaceProfileComponent.initTask(new WorkspaceTaskModule(this));
    this.workspaceTaskComponent.inject(this);

    this.sqlPane = sqlPane;

    this.profileTaskKey = profileTaskKey;

    this.jXTableCaseQuery = getJXTableCase();

    this.workspaceQueryMetadataPanel = new JPanel(new GridLayout(1, 1));
    this.workspaceQueryMetadataPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    this.chartByQueryGanttPanel = GUIHelper.getJSplitPane(JSplitPane.HORIZONTAL_SPLIT, 10, 120);
    this.chartGanttPanelRealTime = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 250);
    this.chartGanttPanelHistory = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 10, 250);

    this.chartPanelSearch = GUIHelper.getJSplitPane(JSplitPane.VERTICAL_SPLIT, 5, 40);
    this.chartPanelSearch.add(new JPanel(), JSplitPane.TOP);
    this.chartPanelSearch.add(new JPanel(), JSplitPane.BOTTOM);
    this.chartPanelSearch.setDividerLocation(40);

    this.chartJTabbedPane = new ChartJTabbedPane();
    this.chartJTabbedPane.add("Real-time", chartGanttPanelRealTime);
    this.chartJTabbedPane.add("History", chartGanttPanelHistory);
    this.chartJTabbedPane.add("Search", chartPanelSearch);

    this.listCardQueryMetricColumn = new QueryMetricColumnContainer();
    this.listCardQueryMetricColumn.addQueryToCard(jXTableCaseQuery);

    this.chartByQueryGanttPanel.add(listCardQueryMetricColumn.getJXTableCaseQuery().getJScrollPane(), JSplitPane.LEFT);
    this.chartByQueryGanttPanel.add(workspaceQueryMetadataPanel, JSplitPane.RIGHT);

    this.sqlPane.add(chartByQueryGanttPanel, JSplitPane.TOP);
    this.sqlPane.add(chartJTabbedPane, JSplitPane.BOTTOM);

    this.querySelectionHandler =
        new QuerySelectionHandler(jXTableCaseQuery, listCardQueryMetricColumn, workspaceQueryMetadataPanel,
            chartGanttPanelRealTime, chartGanttPanelHistory, chartPanelSearch, chartJTabbedPane, profileTaskKey,
            this.workspaceTaskComponent);
  }

  public void loadSql() {
    jXTableCaseQuery.getJxTable().getColumnExt(0).setVisible(false);
    jXTableCaseQuery.getJxTable().getColumnModel().getColumn(0).setCellRenderer(new GUIHelper.ActiveColumnCellRenderer());

    profileManager.getQueryInfoList(profileTaskKey.getProfileId(), profileTaskKey.getTaskId())
        .forEach(queryInfo -> jXTableCaseQuery.getDefaultTableModel().addRow(new Object[]{queryInfo.getId(), queryInfo.getName()}));

    chartGanttPanelRealTime.add(new JPanel(), JSplitPane.TOP);
    chartGanttPanelRealTime.add(new JPanel(), JSplitPane.BOTTOM);

    chartGanttPanelHistory.add(new JPanel(), JSplitPane.TOP);
    chartGanttPanelHistory.add(new JPanel(), JSplitPane.BOTTOM);

    chartGanttPanelRealTime.setDividerLocation(250);
  }

  private JXTableCase getJXTableCase() {
    JXTableCase jxTableCase = GUIHelper.getJXTableCase(5,
        new String[]{QueryColumnNames.ID.getColName(), QueryColumnNames.FULL_NAME.getColName()});
    jxTableCase.getJxTable().getTableHeader().setVisible(true);
    jxTableCase.getJxTable().setSortable(false);

    return jxTableCase;
  }

}
