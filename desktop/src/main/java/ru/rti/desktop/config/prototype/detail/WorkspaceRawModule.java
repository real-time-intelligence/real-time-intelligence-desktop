package ru.rti.desktop.config.prototype.detail;

import dagger.Module;
import dagger.Provides;
import org.fbase.core.FStore;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.detail.RawDataPanel;

import javax.inject.Named;

@Module
public class WorkspaceRawModule {
  private final RawDataPanel rawDataPanel;

  public WorkspaceRawModule(RawDataPanel rawDataPanel) {
    this.rawDataPanel = rawDataPanel;
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
