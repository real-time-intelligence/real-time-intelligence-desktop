package ru.rti.desktop.model.view;

public enum ChartModeType {
  AUTO("Auto"),
  MANUAL("Manual");

  private final String name;

  ChartModeType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
