package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;

import java.time.LocalDate;
import java.util.Optional;

/**
 * One in-flight bar drag. The task is mutated live for immediate visual feedback (snapped
 * to whole days via the {@link TimeScale}); {@link #finish()} folds the entire gesture into
 * a single undoable command - or nothing if it ended where it started.
 */
public final class BarDragSession {

    public enum Mode { MOVE, RESIZE }

    private final GanttTask task;
    private final TimeScale scale;
    private final double pressX;
    private final Mode mode;
    private final LocalDate startBefore;
    private final int durationBefore;

    public BarDragSession(GanttTask task, TimeScale scale, double pressX, Mode mode) {
        this.task = task;
        this.scale = scale;
        this.pressX = pressX;
        this.mode = mode;
        this.startBefore = task.getStart();
        this.durationBefore = task.getDurationDays();
    }

    /** Applies the gesture's current position to the task, snapped to whole days. */
    public void dragTo(double x) {
        double exact = (x - pressX) / scale.pxPerDay();
        // half a day snaps away from zero in both directions (Math.round biases toward +inf)
        long days = (long) (exact >= 0 ? Math.floor(exact + 0.5) : Math.ceil(exact - 0.5));
        switch (mode) {
            case MOVE -> task.setStart(startBefore.plusDays(days));
            case RESIZE -> task.setDurationDays((int) Math.max(1, durationBefore + days));
        }
    }

    /** One command for the whole drag; empty when nothing actually changed. */
    public Optional<UndoableCommand> finish() {
        if (task.getStart().equals(startBefore) && task.getDurationDays() == durationBefore) {
            return Optional.empty();
        }
        LocalDate startAfter = task.getStart();
        int durationAfter = task.getDurationDays();
        return Optional.of(new UndoableCommand() {
            @Override
            public void execute() {
                task.setStart(startAfter);
                task.setDurationDays(durationAfter);
            }

            @Override
            public void undo() {
                task.setStart(startBefore);
                task.setDurationDays(durationBefore);
            }

            @Override
            public String name() {
                return mode == Mode.MOVE ? "Move Task" : "Resize Task";
            }
        });
    }
}
