package ru.rti.desktop.router.listener;

import ru.rti.desktop.model.view.ConfigState;

public interface ConfigListener {

  void fireShowConfig(ConfigState configState);
}
