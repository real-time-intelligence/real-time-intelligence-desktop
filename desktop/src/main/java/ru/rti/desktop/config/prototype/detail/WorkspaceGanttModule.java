package ru.rti.desktop.config.prototype.detail;

import dagger.Module;
import dagger.Provides;
import org.fbase.core.FStore;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.detail.gantt.MainGanttPanel;

import javax.inject.Named;
import java.util.concurrent.ScheduledExecutorService;

@Module
public class WorkspaceGanttModule {
  private final MainGanttPanel mainGanttPanel;

  public WorkspaceGanttModule(MainGanttPanel mainGanttPanel) {
    this.mainGanttPanel = mainGanttPanel;
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

  @WorkspaceDetailScope
  @Provides
  public ScheduledExecutorService getScheduledExecutorService(@Named("executorService") ScheduledExecutorService executorService) {
    return executorService;
  }

}
