package com.richardl.wintak.testutil;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Runs test actions on the JavaFX Application Thread and waits for the result. */
public final class FxThread {

    private FxThread() {
    }

    public static <T> T call(Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    public static void run(Runnable action) throws Exception {
        call(() -> {
            action.run();
            return null;
        });
    }
}
