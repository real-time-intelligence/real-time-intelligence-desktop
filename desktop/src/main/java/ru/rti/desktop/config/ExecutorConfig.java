package ru.rti.desktop.config;

import dagger.Module;
import dagger.Provides;
import ru.rti.desktop.executor.TaskExecutorPool;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Module
public class ExecutorConfig {

  @Provides
  @Singleton
  @Named("executorService")
  public ScheduledExecutorService getScheduledExecutorService() {
    return Executors.newScheduledThreadPool(10);
  }

  @Provides
  @Singleton
  @Named("taskExecutorPool")
  public TaskExecutorPool getTaskExecutorPool() {
    return new TaskExecutorPool();
  }
}
