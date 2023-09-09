package ru.rti.desktop.config.prototype.query;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import org.fbase.core.FStore;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.config.prototype.WorkspaceQueryScope;
import ru.rti.desktop.manager.ConfigurationManager;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.structure.workspace.query.WorkspaceQueryView;

@Module
public class WorkspaceQueryModule {
  private final WorkspaceQueryView workspaceQueryView;

  public WorkspaceQueryModule(WorkspaceQueryView workspaceQueryView) {
    this.workspaceQueryView = workspaceQueryView;
  }

  @WorkspaceQueryScope
  @Provides
  public EventListener provideEventListener(@Named("eventListener") EventListener eventListener) {
    return eventListener;
  }

  @WorkspaceQueryScope
  @Provides
  public ProfileManager provideProfileManager(@Named("profileManager") ProfileManager profileManager) {
    return profileManager;
  }

  @WorkspaceQueryScope
  @Provides
  public ConfigurationManager provideConfigurationManager(@Named("configurationManager") ConfigurationManager configurationManager) {
    return configurationManager;
  }

  @WorkspaceQueryScope
  @Provides
  public AppCache provideAppCache(@Named("appCache") AppCache appCache) {
    return appCache;
  }

  @WorkspaceQueryScope
  @Provides
  public FStore provideFStore(@Named("localDB") FStore fStore) {
    return fStore;
  }
}
