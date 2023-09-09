package ru.rti.desktop.model.sql;

public enum GatherDataSql {
  BY_CLIENT("by_client"),
  BY_CLIENT_PIVOT("by_client_pivot"),
  BY_SERVER("by_server"),
  AD_HOC("ad_hoc");

  private final String description;

  GatherDataSql(String description) {
    this.description = description;
  }

}
