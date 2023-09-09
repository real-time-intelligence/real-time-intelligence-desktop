package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.view.ProgressbarState;

public interface ProgressbarListener {

  void fireProgressbarVisible(ProgressbarState progressbarState);
}
