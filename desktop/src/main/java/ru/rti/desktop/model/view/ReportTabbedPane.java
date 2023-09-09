package ru.rti.desktop.model.view;

public enum ReportTabbedPane {
  DESIGN("Design"),
  REPORT("Report");

  private final String name;

  ReportTabbedPane(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

}
