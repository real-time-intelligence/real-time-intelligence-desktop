package ru.rti.desktop.model.column;

public enum MetricsColumnNames {
  ID("id"),
  NAME("Name"),
  IS_DEFAULT("Default"),
  X_AXIS("X Axis"),
  Y_AXIS("Y Axis"),
  GROUP("Group"),
  METRIC_FUNCTION("Metric function"),
  CHART_TYPE("Chart type");


  private final String colName;

  MetricsColumnNames(String colName) {
    this.colName = colName;
  }

  public String getColName() {
    return this.colName;
  }
}
