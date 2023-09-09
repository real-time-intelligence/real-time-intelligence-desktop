package ru.rti.desktop.config.prototype.task;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceTaskScope;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryComponent;
import ru.rti.desktop.config.prototype.query.WorkspaceQueryModule;
import ru.rti.desktop.view.structure.workspace.task.WorkspaceTaskView;

@WorkspaceTaskScope
@Subcomponent(modules = WorkspaceTaskModule.class)
public interface WorkspaceTaskComponent {
  void inject(WorkspaceTaskView workspaceTaskView);

  WorkspaceQueryComponent initQuery(WorkspaceQueryModule workspaceQueryModule);
}