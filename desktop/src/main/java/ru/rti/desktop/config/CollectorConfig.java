package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.collector.Collector;
import ru.rti.desktop.collector.CollectorImpl;

import javax.inject.Named;

@Module
public abstract class CollectorConfig {

  @Binds
  @Named("collector")
  public abstract Collector bindCollector(CollectorImpl router);
}
