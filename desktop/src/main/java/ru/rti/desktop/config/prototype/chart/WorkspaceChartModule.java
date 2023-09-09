package ru.rti.desktop.config.prototype.chart;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import org.fbase.core.FStore;
import ru.rti.desktop.config.prototype.WorkspaceChartScope;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.state.SqlQueryState;
import ru.rti.desktop.view.chart.search.SearchStackChartPanel;
import ru.rti.desktop.view.chart.stacked.StackChartPanel;

@Module
public class WorkspaceChartModule {
  private final StackChartPanel stackChartPanel;
  private final SearchStackChartPanel searchStackChartPanel;

  public WorkspaceChartModule(StackChartPanel stackChartPanel) {
    this.stackChartPanel = stackChartPanel;
    this.searchStackChartPanel = null;
  }

  public WorkspaceChartModule(SearchStackChartPanel searchStackChartPanel) {
    this.stackChartPanel = null;
    this.searchStackChartPanel = searchStackChartPanel;
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
