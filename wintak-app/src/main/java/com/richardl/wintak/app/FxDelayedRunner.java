package com.richardl.wintak.app;

import com.richardl.wintak.controller.DelayedRunner;
import javafx.animation.PauseTransition;

import java.time.Duration;

/** FX-thread-friendly delayed execution via PauseTransition (no extra threads to marshal). */
public final class FxDelayedRunner implements DelayedRunner {

    private PauseTransition current;

    @Override
    public void runLater(Duration delay, Runnable action) {
        if (current != null) current.stop();
        current = new PauseTransition(javafx.util.Duration.millis(delay.toMillis()));
        current.setOnFinished(e -> action.run());
        current.play();
    }
}
