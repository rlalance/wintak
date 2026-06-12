package com.richardl.wintak.controller;

import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.StatusBar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class StatusMessageTest {

    /** Captures scheduled clears instead of waiting on a real clock. */
    private static final class StubScheduler implements DelayedRunner {
        final List<Runnable> pending = new ArrayList<>();
        Duration lastDelay;

        @Override
        public void runLater(Duration delay, Runnable action) {
            lastDelay = delay;
            pending.add(action);
        }
    }

    @Test
    void setStatusShowsTheMessageAndSchedulesItsClear() {
        MainController controller = new MainController();
        StubScheduler scheduler = new StubScheduler();
        controller.setDelayedRunner(scheduler);

        controller.setStatus("Saved plan.json");

        assertEquals("Saved plan.json", controller.statusMessageProperty().get());
        assertEquals(1, scheduler.pending.size());
        assertFalse(scheduler.lastDelay.isZero());

        scheduler.pending.get(0).run();
        assertEquals("", controller.statusMessageProperty().get());
    }

    @Test
    void aStaleClearNeverWipesANewerMessage() {
        MainController controller = new MainController();
        StubScheduler scheduler = new StubScheduler();
        controller.setDelayedRunner(scheduler);

        controller.setStatus("first");
        controller.setStatus("second");

        scheduler.pending.get(0).run();
        assertEquals("second", controller.statusMessageProperty().get(),
                "the first message's expiry must not clear the second");

        scheduler.pending.get(1).run();
        assertEquals("", controller.statusMessageProperty().get());
    }

    @Test
    void statusBarMessageSegmentIsBound() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            controller.setDelayedRunner((delay, action) -> { });
            StatusBar bar = new StatusBar();
            bar.bindTo(controller);

            controller.setStatus("Working\u2026");
            assertEquals("Working\u2026", bar.messageLabel().getText());
        });
    }

    @Test
    void fileCommandsReportToTheStatusLine() {
        MainController controller = new MainController();
        StubScheduler scheduler = new StubScheduler();
        controller.setDelayedRunner(scheduler);
        controller.newDocument();
        assertTrue(controller.statusMessageProperty().get().length() > 0,
                "New should announce itself in the status line");
    }
}
