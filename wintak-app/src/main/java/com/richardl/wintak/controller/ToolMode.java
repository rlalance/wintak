package com.richardl.wintak.controller;

/** The editing tools on the left toolbar - exactly one is active at a time. */
public enum ToolMode {
    SELECT("Select", "Pick and move tasks"),
    ADD_TASK("Add Task", "Click an empty row to create a task"),
    ADD_MILESTONE("Add Milestone", "Click to create a zero-length milestone"),
    LINK("Link", "Drag between bars to create a dependency"),
    PAN("Pan", "Drag to scroll the timeline");

    private final String label;
    private final String tooltip;

    ToolMode(String label, String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

    public String label() {
        return label;
    }

    public String tooltip() {
        return tooltip;
    }
}
