package ru.rti.desktop.model.view;

public enum TemplateState {
  SHOW("Show"),
  HIDE("Hide");

  private final String description;

  TemplateState(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }

}
