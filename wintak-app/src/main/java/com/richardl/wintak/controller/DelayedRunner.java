package com.richardl.wintak.controller;

import java.time.Duration;

/** Seam around "do this later" so timed behaviour (status auto-clear) is testable. */
@FunctionalInterface
public interface DelayedRunner {

    void runLater(Duration delay, Runnable action);

    /** Null object: never runs the action (messages simply stay until replaced). */
    DelayedRunner NONE = (delay, action) -> { };
}
