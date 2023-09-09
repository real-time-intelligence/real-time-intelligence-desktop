package ru.rti.desktop.config.prototype.detail;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.view.detail.DetailPanel;

@WorkspaceDetailScope
@Subcomponent(modules = WorkspaceDetailModule.class)
public interface WorkspaceDetailComponent {
  void inject(DetailPanel detailPanel);
}