package ru.rti.desktop.collector;

import ru.rti.desktop.collector.loader.DataLoader;
import ru.rti.desktop.model.info.ProfileInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;

import java.sql.Connection;
import java.util.HashMap;

public interface Collector {

  void fillMetadata(QueryInfo queryInfo, TableInfo tableInfo, Connection connection);

  void collect(ProfileInfo profileInfo, TaskInfo taskInfo, HashMap<Integer, DataLoader> loaderHashMap);
}
