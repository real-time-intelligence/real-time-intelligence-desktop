package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.state.NavigatorState;
import ru.rti.desktop.state.SqlQueryState;
import ru.rti.desktop.state.impl.NavigatorStateImpl;
import ru.rti.desktop.state.impl.SqlQueryStateImpl;

import javax.inject.Named;

@Module
public abstract class StateConfig {

  @Binds
  @Named("navigatorState")
  public abstract NavigatorState bindNavigatorState(NavigatorStateImpl navigatorState);

  @Binds
  @Named("sqlQueryState")
  public abstract SqlQueryState bindSqlQueryState(SqlQueryStateImpl sqlQueryState);
}
