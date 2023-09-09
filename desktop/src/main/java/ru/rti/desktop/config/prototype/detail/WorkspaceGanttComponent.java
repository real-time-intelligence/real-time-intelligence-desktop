package ru.rti.desktop.config.prototype.detail;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.view.detail.GanttDataPanel;

@WorkspaceDetailScope
@Subcomponent(modules = WorkspaceGanttModule.class)
public interface WorkspaceGanttComponent {
  void inject(GanttDataPanel ganttDataPanel);
}