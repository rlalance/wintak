package com.richardl.wintak.controller;

import java.time.Duration;

/**
 * Coalesces a burst of "document changed" signals into a single save call after a quiet
 * period. Call {@link #notifyChanged()} on every mutation and {@link #flush()} before exit.
 * Both the delay and save callback are injected so the class is unit-testable without timers.
 */
public class AutoSaveCoalescer {

    /** No-op instance: notifyChanged and flush are both inert. */
    public static final AutoSaveCoalescer NONE =
            new AutoSaveCoalescer(Duration.ZERO, () -> {}, DelayedRunner.NONE) {
                @Override public void notifyChanged() {}
                @Override public void flush() {}
            };

    private final Duration delay;
    private final Runnable saveCallback;
    private final DelayedRunner scheduler;
    private long generation = 0;
    private boolean pending = false;

    public AutoSaveCoalescer(Duration delay, Runnable saveCallback, DelayedRunner scheduler) {
        this.delay = delay;
        this.saveCallback = saveCallback;
        this.scheduler = scheduler;
    }

    /**
     * Signal that the document has changed. The save callback will be invoked after
     * {@code delay} unless another call arrives first (in which case the timer resets).
     */
    public void notifyChanged() {
        pending = true;
        long token = ++generation;
        scheduler.runLater(delay, () -> {
            if (token == generation) {
                pending = false;
                saveCallback.run();
            }
        });
    }

    /**
     * If a save is pending (notifyChanged was called but the delay has not yet elapsed),
     * cancel it and run the save immediately. No-op when nothing is pending.
     * Call this at application exit to ensure no changes are lost.
     */
    public void flush() {
        if (!pending) return;
        generation++;   // invalidate the outstanding scheduled action
        pending = false;
        saveCallback.run();
    }
}
