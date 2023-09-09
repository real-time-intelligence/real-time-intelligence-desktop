package ru.rti.desktop.model.view.workspase;

public enum RelativeDatePane {
    SECONDS("Seconds ago"),
    MINUTES("Minutes ago"),
    HOURS("Hours ago"),
    DAYS("Days ago");

    private final String name;

    RelativeDatePane(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
