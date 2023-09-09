package ru.rti.desktop.executor;

import lombok.extern.log4j.Log4j2;
import ru.rti.desktop.model.ProfileTaskKey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Singleton
public class TaskExecutorPool {

  private final Map<ProfileTaskKey, TaskExecutor> taskExecutorMap = new ConcurrentHashMap<>();

  @Inject
  public TaskExecutorPool() {}

  public void addTaskExecutor(ProfileTaskKey profileTaskKey, TaskExecutor taskExecutor) {
    taskExecutorMap.put(profileTaskKey, taskExecutor);
  }

  public void removeTaskExecutor(ProfileTaskKey profileTaskKey) {
    taskExecutorMap.entrySet()
        .stream()
        .filter(f -> f.getKey().equals(profileTaskKey))
        .findAny()
        .ifPresentOrElse(taskExecutorEntry -> taskExecutorEntry.getValue().stopTask(),
            () -> log.info("Not found running Task Executor for {}", profileTaskKey));

    taskExecutorMap.remove(profileTaskKey);
  }
}
