package ru.rti.desktop.manager.impl;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.helper.GsonHelper;
import ru.rti.desktop.manager.TemplateManager;
import ru.rti.desktop.model.config.Connection;
import ru.rti.desktop.model.config.Query;
import ru.rti.desktop.model.config.Task;

@Log4j2
@Singleton
public class TemplateManagerImpl implements TemplateManager {
  private final GsonHelper gsonHelper;

  @Inject
  public TemplateManagerImpl(GsonHelper gsonHelper) {
    this.gsonHelper = gsonHelper;

    List<Task> taskList = gsonHelper.getConfigListResources(Task.class);
    List<Connection> connectionList = gsonHelper.getConfigListResources(Connection.class);
    List<Query> queryList = gsonHelper.getConfigListResources(Query.class);
  }

  @Override
  public <T> List<T> getConfigList(Class<T> clazz) {
    return gsonHelper.getConfigListResources(clazz);
  }

  @Override
  public List<Query> getQueryListByConnDriver(String connDriver) {
    List<Task> taskListTemplate = gsonHelper.getConfigListResources(Task.class);
    List<Connection> connListTemplate = gsonHelper.getConfigListResources(Connection.class);
    List<Query> queryListTemplate = gsonHelper.getConfigListResources(Query.class);

    return taskListTemplate.stream()
        .filter(task -> connListTemplate.stream()
            .filter(conn -> conn.getDriver().equalsIgnoreCase(connDriver))
            .findAny()
            .orElse(new Connection())
            .getId() == task.getConnectionId())
        .flatMap(t -> t.getQueryList().stream())
        .distinct()
        .map(queryId -> queryListTemplate.stream()
            .filter(q -> q.getId() == queryId)
            .findFirst()
            .orElseThrow())
        .toList();
  }

}
