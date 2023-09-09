package ru.rti.desktop.config.prototype.search;

import dagger.Module;
import dagger.Provides;
import org.fbase.core.FStore;
import ru.rti.desktop.config.prototype.WorkspaceChartScope;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.state.SqlQueryState;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;

import javax.inject.Named;

@Module
public class WorkspaceSearchModule {
  private final StackChartPanel stackChartPanel;

  public WorkspaceSearchModule(StackChartPanel stackChartPanel) {
    this.stackChartPanel = stackChartPanel;
  }

  @WorkspaceChartScope
  @Provides
  public EventListener provideEventListener(@Named("eventListener") EventListener eventListener) {
    return eventListener;
  }

  @WorkspaceChartScope
  @Provides
  public FStore provideFStore(@Named("localDB") FStore fStore) {
    return fStore;
  }

  @WorkspaceChartScope
  @Provides
  public SqlQueryState provideSqlQueryState(@Named("sqlQueryState") SqlQueryState sqlQueryState) {
    return sqlQueryState;
  }

  @WorkspaceChartScope
  @Provides
  public ProfileManager provideProfileManager(@Named("profileManager") ProfileManager profileManager) {
    return profileManager;
  }
}
