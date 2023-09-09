package ru.rti.desktop.model.local;

public enum LoadDataMode {
  DIRECT("direct"),
  JDBC_REAL_TIME("jdbc_real_time"),
  JDBC_BATCH("jdbc_batch");

  private final String description;

  LoadDataMode(String description) {
    this.description = description;
  }

}
