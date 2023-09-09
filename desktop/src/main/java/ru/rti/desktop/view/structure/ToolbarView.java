package ru.rti.desktop.view.structure;

import ru.rti.desktop.model.view.ToolbarButtonState;
import ru.rti.desktop.view.common.BaseView;

public interface ToolbarView extends BaseView {

  void setProfileButtonState(ToolbarButtonState toolbarButtonState);
}
