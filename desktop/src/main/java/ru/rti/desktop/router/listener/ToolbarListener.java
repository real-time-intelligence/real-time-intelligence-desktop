package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.view.ToolbarButtonState;

public interface ToolbarListener {

  void fireToolbarButtonStateChange(ToolbarButtonState toolbarButtonState);
}
