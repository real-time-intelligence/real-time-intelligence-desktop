package ru.rti.desktop.manager.impl;

import static ru.rti.desktop.model.db.DBType.MSSQL;
import static ru.rti.desktop.model.db.DBType.ORACLE;
import static ru.rti.desktop.model.db.DBType.POSTGRES;
import static ru.rti.desktop.model.db.DBType.UNKNOWN;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import org.fbase.core.FStore;
import org.fbase.exception.TableNameEmptyException;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.collector.loader.JdbcLoader;
import ru.rti.desktop.exception.NotFoundException;
import ru.rti.desktop.manager.ConfigurationManager;
import ru.rti.desktop.manager.ConnectionPoolManager;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.config.Profile;
import ru.rti.desktop.model.config.Query;
import ru.rti.desktop.model.config.Table;
import ru.rti.desktop.model.config.Task;
import ru.rti.desktop.model.db.DBType;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;
import ru.rti.desktop.model.view.RangeChartHistory;

@Log4j2
@Singleton
public class ProfileManagerImpl implements ProfileManager, JdbcLoader {

  private final ConfigurationManager configurationManager;
  private final ConnectionPoolManager connectionPoolManager;
  private final FStore fStore;
  private final AppCache appCache;

  @Inject
  public ProfileManagerImpl(@Named("configurationManager") ConfigurationManager configurationManager,
                            @Named("connectionPoolManager") ConnectionPoolManager connectionPoolManager,
                            @Named("localDB") FStore fStore,
                            @Named("appCache") AppCache appCache) {
    this.configurationManager = configurationManager;
    this.connectionPoolManager = connectionPoolManager;
    this.fStore = fStore;
    this.appCache = appCache;

    this.loadDataToCache();
  }

  /** Get by id **/
  @Override
  public ProfileInfo getProfileInfoById(int profileId) {
    return this.appCache.getProfileInfo(profileId);
  }

  @Override
  public TaskInfo getTaskInfoById(int taskId) {
    return this.appCache.getTaskInfo(taskId);
  }

  @Override
  public ConnectionInfo getConnectionInfoById(int connectionId) {
    return this.appCache.getConnectionInfo(connectionId);
  }

  @Override
  public QueryInfo getQueryInfoById(int queryId) {
    return this.appCache.getQueryInfo(queryId);
  }

  /** Get list **/
  @Override
  public List<ProfileInfo> getProfileInfoList() {
    return List.copyOf(this.appCache.getProfileInfoMap().values());
  }

  @Override
  public List<TaskInfo> getTaskInfoList() {
    return new ArrayList<>(appCache.getTaskInfoMap().values());
  }

  @Override
  public List<ConnectionInfo> getConnectionInfoList() {
    return new ArrayList<>(appCache.getConnectionInfo().values());
  }

  @Override
  public List<QueryInfo> getQueryInfoList() {
    return new ArrayList<>(appCache.getQueryInfo().values());
  }

  @Override
  public List<TableInfo> getTableInfoList() {
    return new ArrayList<>(appCache.getTableInfo().values());
  }

  @Override
  public List<ChartInfo> getChartInfoList() {
    return new ArrayList<>(appCache.getChartInfo().values());
  }

  @Override
  public void addProfile(ProfileInfo profileInfo) {
    Profile profile = new Profile();
    profile.setId(profileInfo.getId());
    profile.setName(profileInfo.getName());
    profile.setDescription(profileInfo.getDescription());
    profile.setTaskList(profileInfo.getTaskInfoList());

    configurationManager.addConfig(profile, Profile.class);
    appCache.putProfileInfo(profileInfo);
  }

  @Override
  public void addTask(TaskInfo taskInfo) {
    Task task = new Task();
    task.setId(taskInfo.getId());
    task.setName(taskInfo.getName());
    task.setDescription(taskInfo.getDescription());
    task.setConnectionId(taskInfo.getConnectionId());
    task.setPullTimeout(taskInfo.getPullTimeout());
    task.setQueryList(taskInfo.getQueryInfoList());

    configurationManager.addConfig(task, Task.class);
    appCache.putTaskInfo(taskInfo);
  }

  @Override
  public void addConnection(ConnectionInfo connectionInfo) {
    Connection connection = new Connection();
    connection.setId(connectionInfo.getId());
    connection.setName(connectionInfo.getName());
    connection.setUserName(connectionInfo.getUserName());
    connection.setPassword(connectionInfo.getPassword());
    connection.setUrl(connectionInfo.getUrl());
    connection.setDriver(connectionInfo.getDriver());
    connection.setJar(connectionInfo.getJar());

    configurationManager.addConfig(connection, Connection.class);
    appCache.putConnectionInfo(connectionInfo);
  }

  @Override
  public void addQuery(QueryInfo queryInfo) {
    Query query = new Query();
    query.setId(queryInfo.getId());
    query.setName(queryInfo.getName());
    query.setDescription(queryInfo.getDescription());
    query.setText(queryInfo.getText());
    query.setLoadDataMode(queryInfo.getLoadDataMode());
    query.setGatherDataSql(queryInfo.getGatherDataSql());
    query.setMetricList(queryInfo.getMetricList());

    configurationManager.addConfig(query, Query.class);
    appCache.putQueryInfo(queryInfo);
  }

  @Override
  public void addTable(TableInfo tableInfo) {
    Table table = new Table();
    table.setTableName(tableInfo.getTableName());
    table.setTableType(tableInfo.getTableType());
    table.setIndexType(tableInfo.getIndexType());
    table.setCompression(tableInfo.getCompression());
    table.setCProfiles(tableInfo.getCProfiles());
    table.setValuableColumnList(tableInfo.getValuableColumnList());

    configurationManager.addConfig(table, Table.class);
    appCache.putTableInfo(tableInfo);
  }

  @Override
  public void addChart(ChartInfo chartInfo) {
    appCache.putChartInfo(chartInfo);
  }

  /** Update **/
  @Override
  public void updateProfile(ProfileInfo profileInfo) {
    Profile profile = configurationManager.getConfig(Profile.class, profileInfo.getName());
    profile.setDescription(profile.getDescription());
    profile.setTaskList(profileInfo.getTaskInfoList());

    configurationManager.updateConfig(profile, Profile.class);
    appCache.putProfileInfo(profileInfo);
  }

  @Override
  public void updateTask(TaskInfo taskInfo) {
    Task task = configurationManager.getConfig(Task.class, taskInfo.getName());
    task.setDescription(taskInfo.getDescription());
    task.setPullTimeout(taskInfo.getPullTimeout());
    task.setConnectionId(taskInfo.getConnectionId());
    task.setQueryList(taskInfo.getQueryInfoList());

    configurationManager.updateConfig(task, Task.class);
    appCache.putTaskInfo(taskInfo);
  }

  @Override
  public void updateConnection(ConnectionInfo connectionInfo) {
    Connection connection = configurationManager.getConfig(Connection.class, connectionInfo.getName());
    connection.setUserName(connectionInfo.getUserName());
    connection.setPassword(connectionInfo.getPassword());
    connection.setUrl(connectionInfo.getUrl());
    connection.setJar(connectionInfo.getJar());
    connection.setDriver(connectionInfo.getDriver());

    connectionInfo.setDbType(getDBType(connection.getUrl()));

    configurationManager.updateConfig(connection, Connection.class);
    appCache.putConnectionInfo(connectionInfo);
  }

  @Override
  public void updateQuery(QueryInfo queryInfo) {
    Query query = configurationManager.getConfig(Query.class, queryInfo.getName());
    query.setDescription(queryInfo.getDescription());
    query.setText(queryInfo.getText());
    query.setMetricList(queryInfo.getMetricList());
    configurationManager.updateConfig(query, Query.class);

    this.appCache.putQueryInfo(queryInfo);
  }

  @Override
  public void updateTable(TableInfo tableInfo) {
    Table table = configurationManager.getConfig(Table.class, tableInfo.getTableName());
    table.setCompression(tableInfo.getCompression());
    table.setTableType(tableInfo.getTableType());
    table.setIndexType(tableInfo.getIndexType());
    table.setCProfiles(tableInfo.getCProfiles());
    table.setValuableColumnList(tableInfo.getValuableColumnList());

    configurationManager.updateConfig(table, Table.class);
    appCache.putTableInfo(tableInfo);
  }

  @Override
  public void updateChart(ChartInfo chartInfo) {
    this.appCache.putChartInfo(chartInfo);
  }

  @Override
  public void deleteProfile(int profileId, String profileName) {
    configurationManager.deleteConfig(profileName, Profile.class);
    appCache.deleteProfileInfo(profileId);
  }

  @Override
  public void deleteTask(int taskId, String taskName) {
    configurationManager.deleteConfig(taskName, Task.class);
    appCache.deleteTaskInfo(taskId);
  }

  @Override
  public void deleteConnection(int connectionId, String connectionName) {
    configurationManager.deleteConfig(connectionName, Connection.class);
    appCache.deleteConnectionInfo(connectionId);
  }

  @Override
  public void deleteQuery(int queryId, String queryName) {
    configurationManager.deleteConfig(queryName, Query.class);
    appCache.deleteQueryInfo(queryId);
  }

  @Override
  public void deleteTable(String tableName) {
    configurationManager.deleteConfig(tableName, Table.class);
    appCache.deleteTableInfo(tableName);
  }

  @Override
  public void deleteChart(int chartId) {
    appCache.deleteChartInfo(chartId);
  }

  @Override
  public List<QueryInfo> getQueryInfoList(int profileId, int taskId) {
    return this.appCache.getTaskInfo(taskId)
        .getQueryInfoList().stream()
        .map(this.appCache::getQueryInfo)
        .toList();
  }

  private void loadDataToCache() {
    loadProfileInfoToCache();
    loadTaskInfoToCache();
    loadConnectionInfoToCache();
    loadQueryInfoToCache();
    loadTableInfoToCache();
  }

  private void loadProfileInfoToCache() {
    List<Integer> profileInfoRunning = this.appCache.getProfileInfoMap().values().stream()
        .filter(profileInfo -> RunStatus.RUNNING.equals(profileInfo.getStatus()))
        .map(ProfileInfo::getId)
        .toList();

    this.appCache.clearProfileInfo();

    this.configurationManager
        .getConfigList(Profile.class)
        .forEach(profile -> {
          ProfileInfo profileInfo = new ProfileInfo()
              .setId(profile.getId())
              .setName(profile.getName())
              .setDescription(profile.getDescription())
              .setStatus(profileInfoRunning.contains(profile.getId()) ? RunStatus.RUNNING : RunStatus.NOT_RUNNING);

          profileInfo.setTaskInfoList(profile.getTaskList());

          this.appCache.putProfileInfo(profileInfo);
        });
  }

  private void loadTaskInfoToCache() {
    this.appCache.clearTaskInfo();

    Map<Integer, String> queryIdNameMap = this.configurationManager
        .getConfigList(Query.class)
        .stream()
        .collect(Collectors.toMap(Query::getId, Query::getName));

    this.configurationManager
        .getConfigList(Task.class)
        .forEach(task -> {
          TaskInfo taskInfo = new TaskInfo()
              .setId(task.getId())
              .setName(task.getName())
              .setDescription(task.getDescription())
              .setPullTimeout(task.getPullTimeout())
              .setConnectionId(task.getConnectionId())

              .setQueryInfoList(task.getQueryList())
              .setTableInfoList(getTableInfoList(queryIdNameMap, task.getQueryList()).stream().map(TableInfo::getTableName).toList())
              .setChartInfoList(task.getQueryList());

          getChartInfoList(task.getQueryList(), task.getPullTimeout()).forEach(this.appCache::putChartInfo);

          this.appCache.putTaskInfo(taskInfo);
        });
  }

  private void loadConnectionInfoToCache() {
    this.appCache.clearConnectionInfo();

    this.configurationManager
        .getConfigList(Connection.class)
        .forEach(connection -> {
          ConnectionInfo connectionInfo = new ConnectionInfo();
          connectionInfo.setId(connection.getId());
          connectionInfo.setName(connection.getName());
          connectionInfo.setUserName(connection.getUserName());
          connectionInfo.setPassword(connection.getPassword());
          connectionInfo.setUrl(connection.getUrl());
          connectionInfo.setJar(connection.getJar());
          connectionInfo.setDriver(connection.getDriver());
          connectionInfo.setDbType(getDBType(connection.getUrl()));

          this.appCache.putConnectionInfo(connectionInfo);
        });
  }

  private void loadQueryInfoToCache() {
    Map<Integer, Long> queryIdDeltaLocalServerTimeMap = this.appCache.getQueryInfo().values()
        .stream()
        .collect(Collectors.toMap(QueryInfo::getId, QueryInfo::getDeltaLocalServerTime));

    this.appCache.clearQueryInfo();

    this.configurationManager
        .getConfigList(Query.class)
        .forEach(query -> {
          QueryInfo queryInfo = new QueryInfo();
          queryInfo.setId(query.getId());
          queryInfo.setName(query.getName());
          queryInfo.setText(query.getText());
          queryInfo.setDescription(query.getDescription());
          queryInfo.setGatherDataSql(query.getGatherDataSql());
          queryInfo.setLoadDataMode(query.getLoadDataMode());
          queryInfo.setDeltaLocalServerTime(queryIdDeltaLocalServerTimeMap.getOrDefault(query.getId(), 0L));
          queryInfo.setMetricList(query.getMetricList());

          this.appCache.putQueryInfo(queryInfo);
        });
  }

  private void loadTableInfoToCache() {
    this.appCache.clearTableInfo();

    this.configurationManager
        .getConfigList(Table.class)
        .forEach(table -> {
          TableInfo tableInfo = new TableInfo();
          tableInfo.setTableName(table.getTableName());
          tableInfo.setTableType(table.getTableType());
          tableInfo.setIndexType(table.getIndexType());
          tableInfo.setCompression(table.getCompression());
          tableInfo.setCProfiles(table.getCProfiles());
          tableInfo.setValuableColumnList(table.getValuableColumnList());

          this.appCache.putTableInfo(tableInfo);
        });
  }

  private List<TableInfo> getTableInfoList(Map<Integer, String> queryIdNameMap, List<Integer> queryIdList) {
    List<TableInfo> list = new ArrayList<>();
    queryIdList.forEach(queryId -> {
      TableInfo tableInfo = new TableInfo();
      tableInfo.setTableName(queryIdNameMap.get(queryId));
    });

    return list;
  }

  private List<ChartInfo> getChartInfoList(List<Integer> queryIdList, int pullTimeout) {
    return queryIdList.stream().map(queryId -> {
      ChartInfo chartInfo = new ChartInfo();
      chartInfo.setId(queryId);
      chartInfo.setRange(10);
      chartInfo.setRangeHistory(RangeChartHistory.DAY);
      chartInfo.setPullTimeout(pullTimeout);
      return chartInfo;
    }).toList();
  }

  @Override
  public TableInfo getTableInfoByTableName(String tableName) {
    return this.appCache.getTableInfo(tableName);
  }

  @Override
  public ChartInfo getChartInfoById(int chartId) {
    return this.appCache.getChartInfo(chartId);
  }

  private DBType getDBType(String url) {
    if (url.contains(ORACLE.getUrlPattern())) {
      return ORACLE;
    } else if (url.contains(POSTGRES.getUrlPattern())) {
      return POSTGRES;
    } else if (url.contains(MSSQL.getUrlPattern())) {
      return MSSQL;
    } else {
      return UNKNOWN;
    }
  }

  @Override
  public ProfileInfo getProfileInfoByQueryId(int queryId) {
    return this.appCache.getProfileInfoMap().entrySet().stream()
        .filter(profileInfo -> profileInfo.getValue().getTaskInfoList().stream()
            .anyMatch(taskId -> this.appCache.getTaskInfo(taskId).getQueryInfoList().stream()
                .map(this.appCache::getQueryInfo)
                .anyMatch(queryInfo -> queryInfo.getId() == queryId)))
        .findAny()
        .orElseThrow(() ->
            new NotFoundException(
                String.format("Profile with connected queryId=%s not found", queryId))).getValue();
  }

  @Override
  public void setProfileInfoStatusById(int profileId, RunStatus runStatus) {
    this.appCache.getProfileInfo(profileId).setStatus(runStatus);
  }

  @Override
  public List<QueryInfo> getQueryInfoListByConnDriver(String connDriver) {
    return this.appCache.getTaskInfoMap().values()
        .stream()
        .filter(f -> this.appCache.getConnectionInfo(f.getConnectionId()).getDriver().equalsIgnoreCase(connDriver))
        .flatMap(t -> t.getQueryInfoList().stream())
        .distinct()
        .toList()
        .stream()
        .map(this.appCache::getQueryInfo)
        .toList();
  }

  @Override
  public void updateCache() {
    loadDataToCache();
  }

  @Override
  public void loadDeltaLocalServerTime(ProfileTaskQueryKey profileTaskQueryKey) {
    // TODO move this method to another layer

    TaskInfo taskInfo = appCache.getTaskInfo(profileTaskQueryKey.getTaskId());
    ConnectionInfo connectionInfo = appCache.getConnectionInfo(taskInfo.getConnectionId());
    QueryInfo queryInfo = appCache.getQueryInfo(profileTaskQueryKey.getQueryId());
    TableInfo tableInfo = appCache.getTableInfo(queryInfo.getName());

    try {
      queryInfo.setDbType(connectionInfo.getDbType());
      connectionPoolManager.createDataSource(connectionInfo);
      java.sql.Connection connection = connectionPoolManager.getConnection(connectionInfo);
      long localDateTime = System.currentTimeMillis();
      long serverDateTime = getSysdate(queryInfo.getDbType().getQuery(), connection, log);

      queryInfo.setDeltaLocalServerTime(localDateTime - serverDateTime);

      if (fStore.getTProfile(tableInfo.getTableName()).getCProfiles().isEmpty()) {
        fStore.loadJdbcTableMetadata(connection, queryInfo.getText(), tableInfo.getSProfile());
      }

      updateQuery(queryInfo);
    } catch (SQLException | TableNameEmptyException e) {
      log.info("Error to get connection to fill delta local server time");
      log.catching(e);
    }
  }

}
