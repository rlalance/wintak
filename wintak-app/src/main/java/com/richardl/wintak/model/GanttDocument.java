package com.richardl.wintak.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * The single open document: a project title and its ordered, observable task list.
 * Any mutation - title, task list, dependency list, or a field of a contained task -
 * flips {@link #dirtyProperty()} until {@link #markSaved()}.
 */
public class GanttDocument {

    public static final String DEFAULT_TITLE = "Untitled project";

    private final StringProperty title = new SimpleStringProperty(this, "title", DEFAULT_TITLE);
    private final ObservableList<GanttTask> tasks = FXCollections.observableArrayList();
    private final ObservableList<Dependency> dependencies = FXCollections.observableArrayList();
    private final BooleanProperty dirty = new SimpleBooleanProperty(this, "dirty", false);
    private final InvalidationListener touch = obs -> dirty.set(true);

    public GanttDocument() {
        title.addListener(touch);
        tasks.addListener(touch);
        dependencies.addListener(touch);
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirty;
    }

    /** The current state is the new baseline (just saved or just loaded). */
    public void markSaved() {
        dirty.set(false);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(Objects.requireNonNull(value, "title"));
    }

    public StringProperty titleProperty() {
        return title;
    }

    /** Live list - mutate through the document so invariants stay enforceable. */
    public ObservableList<GanttTask> getTasks() {
        return tasks;
    }

    public void addTask(GanttTask task) {
        Objects.requireNonNull(task, "task");
        if (tasks.contains(task)) {
            throw new IllegalArgumentException("task already in document: " + task);
        }
        tasks.add(task);
        listenTo(task);
    }

    public void removeTask(GanttTask task) {
        if (tasks.remove(task)) {
            unlistenTo(task);
        }
        dependencies.removeIf(d -> d.predecessor() == task || d.successor() == task);
    }

    private void listenTo(GanttTask task) {
        task.nameProperty().addListener(touch);
        task.startProperty().addListener(touch);
        task.durationDaysProperty().addListener(touch);
        task.percentCompleteProperty().addListener(touch);
        task.milestoneProperty().addListener(touch);
    }

    private void unlistenTo(GanttTask task) {
        task.nameProperty().removeListener(touch);
        task.startProperty().removeListener(touch);
        task.durationDaysProperty().removeListener(touch);
        task.percentCompleteProperty().removeListener(touch);
        task.milestoneProperty().removeListener(touch);
    }

    /** Reorders the task at {@code from} to position {@code to}. */
    public void moveTask(int from, int to) {
        GanttTask moved = tasks.remove(from);
        tasks.add(to, moved);
    }

    /** Live list of finish-to-start links - mutate through the document. */
    public ObservableList<Dependency> getDependencies() {
        return dependencies;
    }

    public void addDependency(GanttTask predecessor, GanttTask successor) {
        Dependency link = new Dependency(predecessor, successor);
        if (!tasks.contains(predecessor) || !tasks.contains(successor)) {
            throw new IllegalArgumentException("both tasks must belong to this document");
        }
        if (dependencies.contains(link)) {
            throw new IllegalArgumentException("link already exists: " + link);
        }
        if (reaches(successor, predecessor)) {
            throw new IllegalArgumentException(
                    "link would create a cycle: " + predecessor + " -> " + successor);
        }
        dependencies.add(link);
    }

    public void removeDependency(GanttTask predecessor, GanttTask successor) {
        dependencies.remove(new Dependency(predecessor, successor));
    }

    /** Depth-first walk along successor edges: can we get from {@code from} to {@code target}? */
    private boolean reaches(GanttTask from, GanttTask target) {
        if (from == target) {
            return true;
        }
        return dependencies.stream()
                .filter(d -> d.predecessor() == from)
                .anyMatch(d -> reaches(d.successor(), target));
    }
}
