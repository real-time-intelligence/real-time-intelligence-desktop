package ru.rti.desktop.model.column;

public enum QueryColumnNames {
  ID("id"),
  NAME("Name"),
  FULL_NAME("Query name"),
  GATHER("Gather"),
  MODE("Mode"),
  DESCRIPTION("Description"),
  TEXT("Text");

  private final String colName;

  QueryColumnNames(String colName) {
    this.colName = colName;
  }

  public String getColName() {
    return this.colName;
  }
}
