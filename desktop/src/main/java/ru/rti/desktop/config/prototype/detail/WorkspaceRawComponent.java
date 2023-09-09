package ru.rti.desktop.config.prototype.detail;

import dagger.Subcomponent;
import ru.rti.desktop.config.prototype.WorkspaceDetailScope;
import ru.rti.desktop.view.detail.RawDataPanel;

@WorkspaceDetailScope
@Subcomponent(modules = WorkspaceRawModule.class)
public interface WorkspaceRawComponent {
  void inject(RawDataPanel detailPanel);
}