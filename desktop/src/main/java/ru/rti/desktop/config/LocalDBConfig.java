package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import org.fbase.core.FStore;
import ru.rti.desktop.warehouse.LocalDB;

import javax.inject.Named;

@Module
public abstract class LocalDBConfig {

  @Binds
  @Named("localDB")
  public abstract FStore bindFStore(LocalDB localDB);
}
