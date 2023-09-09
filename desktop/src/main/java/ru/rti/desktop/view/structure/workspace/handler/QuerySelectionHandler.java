package ru.rti.desktop.view.structure.workspace.handler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.config.prototype.task.WorkspaceTaskComponent;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskKey;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.column.QueryColumnNames;
import ru.rti.desktop.model.table.JXTableCase;
import ru.rti.desktop.view.pane.ChartJTabbedPane;
import ru.rti.desktop.view.structure.workspace.query.WorkspaceQueryView;
import ru.rti.desktop.view.structure.workspace.task.QueryMetricColumnContainer;

@Log4j2
public class QuerySelectionHandler implements ListSelectionListener {

  private final JXTableCase jXTableCaseQuery;
  private final JPanel workspaceQueryMetadataPanel;
  private final ProfileTaskKey profileTaskKey;
  private final JSplitPane chartGanttPanelRealTime;
  private final JSplitPane chartGanttPanelHistory;
  private final JSplitPane chartPanelSearch;
  private final ChartJTabbedPane chartJTabbedPane;

  private final QueryMetricColumnContainer listCardQueryMetricColumn;

  private final WorkspaceTaskComponent workspaceTaskComponent;

  @Inject
  @Named("profileManager")
  ProfileManager profileManager;

  public QuerySelectionHandler(JXTableCase jXTableCaseQuery,
                               QueryMetricColumnContainer listCardQueryMetricColumn,
                               JPanel workspaceQueryMetadataPanel,
                               JSplitPane chartGanttPanelRealTime,
                               JSplitPane chartGanttPanelHistory,
                               JSplitPane chartPanelSearch,
                               ChartJTabbedPane chartJTabbedPane,
                               ProfileTaskKey profileTaskKey,
                               WorkspaceTaskComponent workspaceTaskComponent) {
    this.jXTableCaseQuery = jXTableCaseQuery;
    this.jXTableCaseQuery.getJxTable().getSelectionModel().addListSelectionListener(this);

    this.listCardQueryMetricColumn = listCardQueryMetricColumn;

    this.workspaceQueryMetadataPanel = workspaceQueryMetadataPanel;
    this.chartGanttPanelRealTime = chartGanttPanelRealTime;
    this.chartGanttPanelHistory = chartGanttPanelHistory;
    this.chartPanelSearch = chartPanelSearch;
    this.chartJTabbedPane = chartJTabbedPane;

    this.profileTaskKey = profileTaskKey;

    this.workspaceTaskComponent = workspaceTaskComponent;
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();

    // prevents double events
    if (!e.getValueIsAdjusting()) {
      workspaceQueryMetadataPanel.removeAll();

      if (listSelectionModel.isSelectionEmpty()) {
        log.info("Clearing query fields");
      } else {
        int queryId = GUIHelper.getIdByColumnName(this.jXTableCaseQuery.getJxTable(), this.jXTableCaseQuery.getDefaultTableModel(),
            listSelectionModel, QueryColumnNames.ID.getColName());

        ProfileTaskQueryKey profileTaskQueryKey = new ProfileTaskQueryKey(profileTaskKey.getProfileId(),
            profileTaskKey.getTaskId(), queryId);

        WorkspaceQueryView workspaceQueryView = new WorkspaceQueryView(profileTaskQueryKey,
                chartGanttPanelRealTime, chartGanttPanelHistory, chartPanelSearch, chartJTabbedPane,
                listCardQueryMetricColumn, workspaceTaskComponent);

        workspaceQueryMetadataPanel.add(workspaceQueryView);

        chartGanttPanelRealTime.add(new JPanel(), JSplitPane.TOP);
        chartGanttPanelRealTime.add(new JPanel(), JSplitPane.BOTTOM);
        chartGanttPanelRealTime.setDividerLocation(250);

        chartGanttPanelHistory.add(new JPanel(), JSplitPane.TOP);
        chartGanttPanelHistory.add(new JPanel(), JSplitPane.BOTTOM);
        chartGanttPanelHistory.setDividerLocation(250);

        workspaceQueryMetadataPanel.repaint();
        workspaceQueryMetadataPanel.revalidate();

        log.info("Query id: " + queryId);
      }
    }
  }

}
