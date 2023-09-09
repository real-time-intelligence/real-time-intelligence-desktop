package ru.rti.desktop.view.structure.workspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.*;

import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import ru.rti.desktop.collector.Collector;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.executor.TaskExecutor;
import ru.rti.desktop.executor.TaskExecutorPool;
import ru.rti.desktop.manager.ConnectionPoolManager;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ActionName;
import ru.rti.desktop.model.ProfileTaskKey;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.view.ProgressbarState;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.router.listener.ProfileStartStopListener;
import ru.rti.desktop.router.listener.WorkspaceListener;
import ru.rti.desktop.state.NavigatorState;
import ru.rti.desktop.state.SqlQueryState;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;
import ru.rti.desktop.view.structure.WorkspaceView;
import ru.rti.desktop.view.structure.workspace.profile.WorkspaceProfileView;

@Log4j2
@Singleton
public class WorkspacePresenter implements WorkspaceListener, ActionListener, ProfileStartStopListener {

  private final WorkspaceView workspaceView;
  private final NavigatorState navigatorState;
  private final EventListener eventListener;
  private final ProfileManager profileManager;
  private final TaskExecutorPool taskExecutorPool;
  private final ConnectionPoolManager connectionPoolManager;
  private final ScheduledExecutorService executorService;
  private final SqlQueryState sqlQueryState;
  private final Collector collector;
  private final FStore fStore;

  @Inject
  public WorkspacePresenter(@Named("workspaceView") WorkspaceView workspaceView,
                            @Named("navigatorState") NavigatorState navigatorState,
                            @Named("eventListener") EventListener eventListener,
                            @Named("profileManager") ProfileManager profileManager,
                            @Named("taskExecutorPool") TaskExecutorPool taskExecutorPool,
                            @Named("connectionPoolManager") ConnectionPoolManager connectionPoolManager,
                            @Named("executorService") ScheduledExecutorService executorService,
                            @Named("sqlQueryState") SqlQueryState sqlQueryState,
                            @Named("collector") Collector collector,
                            @Named("localDB") FStore fStore) {
    this.workspaceView = workspaceView;
    this.navigatorState = navigatorState;
    this.eventListener = eventListener;
    this.profileManager = profileManager;
    this.taskExecutorPool = taskExecutorPool;
    this.connectionPoolManager = connectionPoolManager;
    this.executorService = executorService;
    this.sqlQueryState = sqlQueryState;
    this.collector = collector;
    this.fStore = fStore;

    this.eventListener.addProfileSelectOnNavigator(this);
    this.eventListener.addProfileStartStopListener(this);
  }

  @Override
  public void fireOnSelectProfileOnNavigator(int profileId) {
    log.info("Fire on select profile on Navigator for profileId (workspace): " + profileId);
    log.info("Check selected profile in navigatorState (workspace): " + navigatorState.getSelectedProfile());

    executorService.submit(() -> {
      eventListener.fireProgressbarVisible(ProgressbarState.SHOW);
      try {
        eventListener.clearListener(WorkspaceProfileView.class);
        eventListener.clearListener(StackChartPanel.class);

        WorkspaceProfileView workspaceProfileView = workspaceView.addWorkspaceProfileView(profileId);
        eventListener.addProfileStartStopListener(workspaceProfileView);
      } catch (Exception e) {
        log.catching(e);
      } finally {
        eventListener.fireProgressbarVisible(ProgressbarState.HIDE);
      }
    });
  }

  @Override
  public void fireOnStartOnWorkspaceProfileView(int profileId) {
    log.info("Fire on start for profileId (workspace -> ProfileView): " + profileId);

  }

  @Override
  public void fireOnStopOnWorkspaceProfileView(int profileId) {
    log.info("Fire on stop for profileId (workspace -> ProfileView): " + profileId);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JButton button = (JButton) e.getSource();
    String actionName = button.getActionCommand();

    executorService.submit(() -> {
      eventListener.fireProgressbarVisible(ProgressbarState.SHOW);
        try {
            actionPerformed(actionName);
        } catch (Exception exception) {
            log.catching(exception);

            ActionName actionNameMessage = ActionName.START;
            if (ActionName.STOP.name().equals(actionName)) {
                actionNameMessage = ActionName.STOP;
            }

            JOptionPane.showMessageDialog(null, exception.getMessage(),
                    "Error for " + actionNameMessage.name() + " action", JOptionPane.ERROR_MESSAGE);

            throw new RuntimeException(exception);
        } finally {
            eventListener.fireProgressbarVisible(ProgressbarState.HIDE);
        }
    });

    log.info("Button " + actionName + " has fired..");
  }

  private void actionPerformed(String actionName) {
    int profileId = navigatorState.getSelectedProfile();

    if (ActionName.START.name().equals(actionName)) {
      profileManager.setProfileInfoStatusById(profileId, RunStatus.RUNNING);
      eventListener.fireOnStartOnWorkspaceProfileView(profileId);

      profileManager.getProfileInfoById(profileId)
          .getTaskInfoList()
          .forEach(taskId -> {
            TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);
            ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(taskInfo.getConnectionId());

            List<QueryInfo> queryInfoList = profileManager.getQueryInfoList().stream()
                .filter(f -> taskInfo.getQueryInfoList().stream().anyMatch(qId -> qId == f.getId()))
                .toList();

            List<TableInfo> tableInfoList = profileManager.getTableInfoList().stream()
                .filter(f -> queryInfoList.stream().anyMatch(qId -> qId.getName().equalsIgnoreCase(f.getTableName())))
                .toList();

            queryInfoList.forEach(queryInfo -> {
              queryInfo.setDbType(connectionInfo.getDbType());
              connectionPoolManager.createDataSource(connectionInfo);

              ProfileTaskQueryKey profileTaskQueryKey = new ProfileTaskQueryKey(profileId, taskId, queryInfo.getId());

              try {
                TableInfo tableInfo = tableInfoList
                    .stream()
                    .filter(f -> f.getTableName().equals(queryInfo.getName())).findAny()
                    .orElseThrow(() -> new NotFoundException("Table not found by name: " + queryInfo.getName()));

                long lastTimestampLocalDb = fStore.getLastTimestamp(tableInfo.getTableName(), Long.MIN_VALUE, Long.MIN_VALUE);
                sqlQueryState.initializeLastTimestamp(profileTaskQueryKey, lastTimestampLocalDb);

                Connection connection = connectionPoolManager.getConnection(connectionInfo);
                collector.fillMetadata(queryInfo, tableInfo, connection);

                profileManager.updateQuery(queryInfo);
              } catch (Exception e) {
                profileManager.setProfileInfoStatusById(profileId, RunStatus.NOT_RUNNING);
                sqlQueryState.clear(profileTaskQueryKey);
                eventListener.fireOnStopOnWorkspaceProfileView(profileId);
                throw new RuntimeException(e);
              }
            });
          });

      profileManager.getProfileInfoById(profileId)
          .getTaskInfoList()
          .forEach(taskId -> {
            TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);

            List<QueryInfo> queryInfoList = profileManager.getQueryInfoList().stream()
                .filter(f -> taskInfo.getQueryInfoList().stream().anyMatch(qId -> qId == f.getId()))
                .toList();

            List<TableInfo> tableInfoList = profileManager.getTableInfoList().stream()
                .filter(f -> queryInfoList.stream().anyMatch(qId -> qId.getName().equalsIgnoreCase(f.getTableName())))
                .toList();

                queryInfoList.forEach(queryInfo -> {

                  TableInfo tableInfo = tableInfoList
                      .stream()
                      .filter(f -> f.getTableName().equals(queryInfo.getName())).findAny()
                      .orElseThrow(() -> new NotFoundException("Table info not found.."));

                  tableInfo.getSProfile().getCsTypeMap()
                      .entrySet()
                      .stream()
                      .filter(f -> f.getValue().isTimeStamp())
                      .findAny()
                      .ifPresentOrElse(csTypeEntry -> log.info("Found timestamp field: " + csTypeEntry.getKey()),
                          () -> {
                            profileManager.setProfileInfoStatusById(profileId, RunStatus.NOT_RUNNING);
                            eventListener.fireOnStopOnWorkspaceProfileView(profileId);
                            throw new NotFoundException(
                                "Not found timestamp field for query: " + queryInfo.getName());
                          });

                  if (tableInfo.getSProfile().getCsTypeMap().isEmpty()) {
                    profileManager.setProfileInfoStatusById(profileId, RunStatus.NOT_RUNNING);
                    eventListener.fireOnStopOnWorkspaceProfileView(profileId);

                    throw new NotFoundException("Metadata for query: " + queryInfo.getName() + " not found..");
                  }
                });
          });

      ProfileInfo profileInfo = profileManager.getProfileInfoById(profileId);

      profileInfo.getTaskInfoList()
          .forEach(taskId -> {
            TaskInfo taskInfo = profileManager.getTaskInfoById(taskId);
            ConnectionInfo connectionInfo = profileManager.getConnectionInfoById(taskInfo.getConnectionId());

            List<QueryInfo> queryInfoList = profileManager.getQueryInfoList().stream()
                    .filter(f -> taskInfo.getQueryInfoList().stream().anyMatch(qId -> qId == f.getId()))
                    .toList();

            List<TableInfo> tableInfoList = profileManager.getTableInfoList().stream()
                .filter(f -> queryInfoList.stream().anyMatch(qId -> qId.getName().equalsIgnoreCase(f.getTableName())))
                .toList();

            log.info("Task name {}", taskInfo.getName());

            ProfileTaskKey profileTaskKey = new ProfileTaskKey(profileId, taskInfo.getId());

            TaskExecutor taskExecutor = new TaskExecutor(connectionPoolManager, collector, profileInfo,
                taskInfo, connectionInfo, queryInfoList, tableInfoList, sqlQueryState, fStore);

            taskExecutor.startTask();

            taskExecutorPool.addTaskExecutor(profileTaskKey, taskExecutor);
          });

    } else if (ActionName.STOP.name().equals(actionName)) {
      profileManager.setProfileInfoStatusById(profileId, RunStatus.NOT_RUNNING);
      eventListener.fireOnStopOnWorkspaceProfileView(profileId);

      profileManager.getProfileInfoById(profileId)
          .getTaskInfoList()
          .forEach(taskId -> {
            log.info("Task id: " + taskId);
            ProfileTaskKey profileTaskKey = new ProfileTaskKey(profileId, taskId);
            taskExecutorPool.removeTaskExecutor(profileTaskKey);
          });
    }
  }

}
