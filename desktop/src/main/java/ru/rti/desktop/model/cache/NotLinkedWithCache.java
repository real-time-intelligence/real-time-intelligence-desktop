package ru.rti.desktop.model.cache;

import lombok.Data;
import ru.rti.desktop.model.info.ConnectionInfo;
import ru.rti.desktop.model.info.QueryInfo;
import ru.rti.desktop.model.info.TableInfo;
import ru.rti.desktop.model.info.TaskInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * taskInfoMap - Map of TaskInfo not linked with any of ProfileInfo
 * connectionInfoMap - Map of ConnectionInfo not linked with any of TaskInfo
 * queryInfoMap - Map of QueryInfo not linked with any of TaskInfo
 * tableInfoMap - Map of TableInfo not linked with any of TaskInfo
 */
@Data
public class NotLinkedWithCache {
    private final Map<Integer, TaskInfo> taskInfoMap = new ConcurrentHashMap<>();
    private final Map<Integer, ConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();
    private final Map<Integer, QueryInfo> queryInfoMap = new ConcurrentHashMap<>();
    private final Map<String, TableInfo> tableInfoMap = new ConcurrentHashMap<>();
}
