package ru.rti.desktop.config.prototype.task;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import ru.rti.desktop.config.prototype.WorkspaceTaskScope;
import ru.rti.desktop.manager.ProfileManager;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.structure.workspace.task.WorkspaceTaskView;

@Module
public class WorkspaceTaskModule {
  private final WorkspaceTaskView workspaceTaskView;

  public WorkspaceTaskModule(WorkspaceTaskView workspaceTaskView) {
    this.workspaceTaskView = workspaceTaskView;
  }

  @WorkspaceTaskScope
  @Provides
  public EventListener provideEventListener(@Named("eventListener") EventListener eventListener) {
    return eventListener;
  }

  @WorkspaceTaskScope
  @Provides
  public ProfileManager provideProfileManager(@Named("profileManager") ProfileManager profileManager) {
    return profileManager;
  }

}
