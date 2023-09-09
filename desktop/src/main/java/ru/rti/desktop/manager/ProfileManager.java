package ru.rti.desktop.manager;

import java.util.List;
import ru.rti.desktop.model.ProfileTaskQueryKey;
import ru.rti.desktop.model.RunStatus;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;
import ru.rti.desktop.model.info.gui.ChartInfo;

public interface ProfileManager {

  /** Get by id **/
  ProfileInfo getProfileInfoById(int profileId);
  TaskInfo getTaskInfoById(int taskId);
  ConnectionInfo getConnectionInfoById(int connectionId);
  QueryInfo getQueryInfoById(int queryId);
  TableInfo getTableInfoByTableName(String tableName);
  ChartInfo getChartInfoById(int chartId);

  /** Get list **/
  List<ProfileInfo> getProfileInfoList();
  List<TaskInfo> getTaskInfoList();
  List<ConnectionInfo> getConnectionInfoList();
  List<QueryInfo> getQueryInfoList();
  List<TableInfo> getTableInfoList();
  List<ChartInfo> getChartInfoList();

  /** Create **/
  void addProfile(ProfileInfo profileInfo);
  void addTask(TaskInfo taskInfo);
  void addConnection(ConnectionInfo connectionInfo);
  void addQuery(QueryInfo queryInfo);
  void addTable(TableInfo tableInfo);
  void addChart(ChartInfo chartInfo);

  /** Update **/
  void updateProfile(ProfileInfo profileInfo);
  void updateTask(TaskInfo taskInfo);
  void updateConnection(ConnectionInfo connectionInfo);
  void updateQuery(QueryInfo queryInfo);
  void updateTable(TableInfo tableInfo);
  void updateChart(ChartInfo chartInfo);

  /** Delete **/
  void deleteProfile(int profileId, String profileName);
  void deleteTask(int taskId, String taskName);
  void deleteConnection(int connectionId, String connectionName);
  void deleteQuery(int queryId, String queryName);
  void deleteTable(String tableName);
  void deleteChart(int chartId);

  List<QueryInfo> getQueryInfoList(int profileId, int taskId);

  ProfileInfo getProfileInfoByQueryId(int queryId);

  void setProfileInfoStatusById(int profileId, RunStatus runStatus);

  List<QueryInfo> getQueryInfoListByConnDriver(String connDriver);

  void updateCache();

  void loadDeltaLocalServerTime(ProfileTaskQueryKey profileTaskQueryKey);
}
