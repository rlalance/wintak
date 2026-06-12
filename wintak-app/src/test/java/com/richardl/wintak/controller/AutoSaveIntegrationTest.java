package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class AutoSaveIntegrationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @TempDir Path tmp;

    @Test
    void autoSaveWithNoFileIsNoop() throws Exception {
        MainController controller = new MainController();
        controller.getDocument().addTask(new GanttTask("t", DAY, 1));
        assertTrue(controller.getDocument().isDirty());

        controller.autoSave().get(1, TimeUnit.SECONDS);

        assertTrue(controller.getDocument().isDirty(), "autoSave with no currentFile must not mark the document clean");
    }

    @Test
    void autoSaveWithFileWritesAndMarksSaved(@TempDir Path dir) throws Exception {
        MainController controller = new MainController();
        controller.getDocument().addTask(new GanttTask("t", DAY, 1));
        Path file = dir.resolve("project.wgp");

        controller.setCurrentFileForTest(file);
        controller.autoSave().get(5, TimeUnit.SECONDS);  // await disk write
        FxThread.run(() -> {});                           // flush FX queue so markSaved fires

        assertFalse(controller.getDocument().isDirty(), "autoSave must mark the document clean");
        assertTrue(file.toFile().exists(), "autoSave must write the file");
    }

    @Test
    void dirtyDocumentWithFileTriggersCoalescerNotify(@TempDir Path dir) {
        AtomicInteger notified = new AtomicInteger();
        List<Runnable> bucket = new ArrayList<>();
        MainController controller = new MainController();
        controller.setCurrentFileForTest(dir.resolve("project.wgp"));
        controller.setAutoSaveCoalescer(new AutoSaveCoalescer(
                Duration.ofSeconds(2), () -> {}, (delay, action) -> { notified.incrementAndGet(); bucket.add(action); }));

        controller.getDocument().addTask(new GanttTask("t", DAY, 1));

        assertEquals(1, notified.get(), "a dirty-making edit with a current file must call notifyChanged once");
    }

    @Test
    void dirtyDocumentWithNoFileDoesNotNotifyCoalescer() {
        AtomicInteger notified = new AtomicInteger();
        MainController controller = new MainController();
        controller.setAutoSaveCoalescer(new AutoSaveCoalescer(
                Duration.ofSeconds(2), () -> {}, (delay, action) -> notified.incrementAndGet()));

        controller.getDocument().addTask(new GanttTask("t", DAY, 1));

        assertEquals(0, notified.get(), "no currentFile -- coalescer must not be notified");
    }

    @Test
    void exitFlushesCoalescer() {
        AtomicInteger flushes = new AtomicInteger();
        MainController controller = new MainController();
        controller.setUserPrompts(UserPrompts.NONE);
        controller.setOnCloseRequest(() -> {});

        AutoSaveCoalescer spy = new AutoSaveCoalescer(Duration.ofSeconds(2), () -> {}, DelayedRunner.NONE) {
            @Override public void flush() { flushes.incrementAndGet(); super.flush(); }
        };
        controller.setAutoSaveCoalescer(spy);

        controller.exit();

        assertEquals(1, flushes.get(), "exit() must call flush() on the coalescer");
    }
}
