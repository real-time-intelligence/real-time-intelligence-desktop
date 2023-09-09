package ru.rti.desktop.config.prototype.profile;

import dagger.Module;
import dagger.Provides;
import ru.rti.desktop.config.prototype.WorkspaceProfileScope;
import ru.rti.desktop.helper.GUIHelper;
import ru.rti.desktop.model.ActionName;
import ru.rti.desktop.router.event.EventListener;
import ru.rti.desktop.view.structure.workspace.profile.WorkspaceProfileView;

import javax.inject.Named;
import javax.swing.*;

@Module
public class WorkspaceProfileModule {
  private final WorkspaceProfileView workspaceProfileView;

  public WorkspaceProfileModule(WorkspaceProfileView workspaceProfileView) {
    this.workspaceProfileView = workspaceProfileView;
  }

  @WorkspaceProfileScope
  @Provides
  public EventListener provideEventListener(@Named("eventListener") EventListener eventListener) {
    return eventListener;
  }

  @WorkspaceProfileScope
  @Provides
  @Named("workspaceProfileStartButton")
  public JButton getStartButton() {
    return GUIHelper.getJButton(ActionName.START.name());
  }

  @WorkspaceProfileScope
  @Provides
  @Named("workspaceProfileStopButton")
  public JButton getStopButton() {
    return GUIHelper.getJButton(ActionName.STOP.name());
  }

  @WorkspaceProfileScope
  @Provides
  @Named("profileStatusJLabel")
  public JLabel getToolbarProfileStatusJLabel() {
    return new JLabel();
  }

}
