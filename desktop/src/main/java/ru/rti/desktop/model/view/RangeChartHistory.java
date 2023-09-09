package ru.rti.desktop.model.view;

public enum RangeChartHistory {
  DAY("Day"),
  WEEK("Week"),
  MONTH("Month"),
  CUSTOM("Custom");

  private final String name;

  RangeChartHistory(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
