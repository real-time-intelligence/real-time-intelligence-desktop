package ru.rti.desktop.model.view;

public enum ProcessType {
  REAL_TIME("Real-time"),
  HISTORY("History"),
  SEARCH("Search");

  private final String name;

  ProcessType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
