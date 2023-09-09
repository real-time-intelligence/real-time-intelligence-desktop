package ru.rti.desktop.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import ru.rti.desktop.collector.Collector;
import ru.rti.desktop.collector.by.ByClient;
import ru.rti.desktop.collector.by.ByServer;
import ru.rti.desktop.collector.by.ByTarget;
import ru.rti.desktop.collector.loader.DataLoader;
import ru.rti.desktop.collector.loader.RowToRowLoader;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.manager.ConnectionPoolManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.sql.GatherDataSql;
import ru.rti.desktop.state.SqlQueryState;

@Log4j2
public class TaskExecutor {

  private final ScheduledExecutorService executorService;

  private final ProfileInfo profileInfo;
  private final TaskInfo taskInfo;
  private final ConnectionInfo connectionInfo;
  private final List<QueryInfo> queryInfoList;
  private final List<TableInfo> tableInfoList;

  private final SqlQueryState sqlQueryState;
  private final Collector collector;

  private final ConnectionPoolManager connectionPoolManager;

  private Connection connection;
  private ScheduledFuture<?> schedule;

  private final int pullTimeout;

  private final HashMap<Integer, DataLoader> loaderHashMap;

  private final FStore fStore;

  /**
   * Task executor
   */
  public TaskExecutor(ConnectionPoolManager connectionPoolManager,
                      Collector collector,
                      ProfileInfo profileInfo,
                      TaskInfo taskInfo,
                      ConnectionInfo connectionInfo,
                      List<QueryInfo> queryInfoList,
                      List<TableInfo> tableInfoList,
                      SqlQueryState sqlQueryState,
                      FStore fStore) {
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.connectionPoolManager = connectionPoolManager;
    this.profileInfo = profileInfo;
    this.taskInfo = taskInfo;
    this.connectionInfo = connectionInfo;
    this.queryInfoList = queryInfoList;
    this.tableInfoList = tableInfoList;
    this.collector = collector;
    this.sqlQueryState = sqlQueryState;
    this.fStore = fStore;

    this.pullTimeout = taskInfo.getPullTimeout();

    this.connection = null;
    this.loaderHashMap = new HashMap<>();
  }

  public void startTask() {
    try {
      connection = connectionPoolManager.getConnection(connectionInfo);

      queryInfoList.forEach(queryInfo -> {
        TableInfo tableInfo = tableInfoList.stream()
            .filter(f -> f.getTableName().equalsIgnoreCase(queryInfo.getName()))
            .findAny()
            .orElseThrow(() -> new NotFoundException("Table info not found.."));

        queryInfo.setDbType(connectionInfo.getDbType());
        try {
          ProfileTaskQueryKey profileTaskQueryKey = new ProfileTaskQueryKey(profileInfo.getId(),
              taskInfo.getId(), queryInfo.getId());

          if (GatherDataSql.BY_CLIENT.equals(queryInfo.getGatherDataSql())) {
            ByTarget byClient = new ByClient(profileTaskQueryKey, queryInfo, connection);
            DataLoader dataLoader = new RowToRowLoader(byClient, connection, profileTaskQueryKey,
                taskInfo, queryInfo, tableInfo, sqlQueryState, fStore);
            loaderHashMap.put(queryInfo.getId(), dataLoader);
          } else if (GatherDataSql.BY_SERVER.equals(queryInfo.getGatherDataSql())) {
            ByTarget byServer = new ByServer(profileTaskQueryKey, queryInfo, tableInfo, connection, sqlQueryState);
            DataLoader dataLoader = new RowToRowLoader(byServer, connection, profileTaskQueryKey,
                taskInfo, queryInfo, tableInfo, sqlQueryState, fStore);
            loaderHashMap.put(queryInfo.getId(), dataLoader);
          }

        } catch (Exception e) {
          log.catching(e);
          throw new RuntimeException(e);
        }
      });
    } catch (SQLException e) {
      log.catching(e);
      throw new RuntimeException(e);
    }

    schedule = executorService.scheduleWithFixedDelay(this::run, pullTimeout, pullTimeout, TimeUnit.SECONDS);
  }

  public void stopTask() {
    executorService.schedule(() -> schedule.cancel(false), 0, TimeUnit.SECONDS);
  }

  private void run() {
    Instant before = Instant.now();
    collector.collect(profileInfo, taskInfo, loaderHashMap);
    Instant after = Instant.now();

    double range = ((double) after.toEpochMilli() - (double) before.toEpochMilli()) / 1000;
    log.info("Task: " + taskInfo.getName() + ", execution (sec): " + range);
  }

}
