package ru.rti.desktop.model.column;

public enum ProfileColumnNames {
  ID("id"),
  NAME("Name");

  private final String colName;

  ProfileColumnNames(String colName) {
    this.colName = colName;
  }

  public String getColName() {
    return this.colName;
  }
}
