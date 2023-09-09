package ru.rti.desktop.model.view;

public enum EditTabbedPane {
  PROFILE("Profile"),
  TASK("Task"),
  CONNECTION("Connection"),
  QUERY("Query");

  private final String name;

  EditTabbedPane(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

}
