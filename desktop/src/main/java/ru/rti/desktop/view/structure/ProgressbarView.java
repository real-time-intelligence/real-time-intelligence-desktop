package ru.rti.desktop.view.structure;

import ru.rti.desktop.model.view.ProgressbarState;
import ru.rti.desktop.view.common.BaseView;

public interface ProgressbarView extends BaseView {

  void setProgressbarVisible(ProgressbarState progressbarState);
}
