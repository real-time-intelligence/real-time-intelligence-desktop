package ru.rti.desktop.model.function;

public enum MetricFunction {
  NONE("None"),
  ASIS("As is"),
  COUNT("Count"),
  SUM("Sum"),
  AVERAGE("Average");

  private final String name;

  MetricFunction(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
