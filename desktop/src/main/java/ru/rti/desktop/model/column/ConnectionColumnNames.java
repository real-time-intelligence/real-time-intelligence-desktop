package ru.rti.desktop.model.column;

public enum ConnectionColumnNames {
  ID("id"),
  NAME("Name"),
  USER_NAME("User name"),
  PASSWORD("password"),
  URL("URL"),
  JAR("jar"),
  DRIVER("driver");

  private final String colName;

  ConnectionColumnNames(String colName) {
    this.colName = colName;
  }

  public String getColName() {
    return this.colName;
  }
}
