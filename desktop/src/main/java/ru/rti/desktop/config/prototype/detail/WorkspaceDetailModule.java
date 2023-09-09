package ru.rti.desktop.config.prototype.detail;

import dagger.Module;
import dagger.Provides;
import org.fbase.core.FStore;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.detail.DetailPanel;

import javax.inject.Named;

@Module
public class WorkspaceDetailModule {
  private final DetailPanel detailPanel;

  public WorkspaceDetailModule(DetailPanel detailPanel) {
    this.detailPanel = detailPanel;
  }

  @WorkspaceDetailScope
  @Provides
  public EventListener provideEventListener(@Named("eventListener") EventListener eventListener) {
    return eventListener;
  }

  @WorkspaceDetailScope
  @Provides
  public FStore provideFStore(@Named("localDB") FStore fStore) {
    return fStore;
  }

}
