package com.richardl.wintak.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * One bar on the chart: an immutable id plus observable name, start date, duration in days
 * (>= 1) and percent-complete (0-100). Invariants are enforced on every write.
 */
public final class GanttTask {

    private final String id;
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final ObjectProperty<LocalDate> start = new SimpleObjectProperty<>(this, "start");
    private final IntegerProperty durationDays = new SimpleIntegerProperty(this, "durationDays");
    private final IntegerProperty percentComplete = new SimpleIntegerProperty(this, "percentComplete");
    private final BooleanProperty milestone = new SimpleBooleanProperty(this, "milestone", false);

    public GanttTask(String name, LocalDate start, int durationDays) {
        this(UUID.randomUUID().toString(), name, start, durationDays);
    }

    /** Restoration constructor (persistence) - preserves the task's identity. */
    public GanttTask(String id, String name, LocalDate start, int durationDays) {
        this.id = Objects.requireNonNull(id, "id");
        setName(name);
        setStart(start);
        setDurationDays(durationDays);
        setPercentComplete(0);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("task name must not be blank");
        }
        name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public LocalDate getStart() {
        return start.get();
    }

    public void setStart(LocalDate value) {
        start.set(Objects.requireNonNull(value, "start"));
    }

    public ObjectProperty<LocalDate> startProperty() {
        return start;
    }

    public int getDurationDays() {
        return durationDays.get();
    }

    public void setDurationDays(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("duration must be >= 1 day, was " + value);
        }
        if (milestone.get() && value != 1) {
            throw new IllegalArgumentException("milestone duration is locked at 1 day");
        }
        durationDays.set(value);
    }

    public IntegerProperty durationDaysProperty() {
        return durationDays;
    }

    public int getPercentComplete() {
        return percentComplete.get();
    }

    public void setPercentComplete(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("percent-complete must be 0..100, was " + value);
        }
        percentComplete.set(value);
    }

    public IntegerProperty percentCompleteProperty() {
        return percentComplete;
    }

    public boolean isMilestone() {
        return milestone.get();
    }

    public void setMilestone(boolean value) {
        if (value && durationDays.get() != 1) {
            throw new IllegalArgumentException("cannot mark a task as milestone when durationDays != 1");
        }
        milestone.set(value);
    }

    public BooleanProperty milestoneProperty() {
        return milestone;
    }

    /** First day after the bar - start + duration. */
    public LocalDate getEndExclusive() {
        return getStart().plusDays(getDurationDays());
    }

    @Override
    public String toString() {
        return "GanttTask[" + getName() + " " + getStart() + " +" + getDurationDays() + "d]";
    }
}
