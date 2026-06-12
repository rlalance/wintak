package com.richardl.wintak.view;

import com.richardl.wintak.controller.FileDialogs;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class StatusBarBindingTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void dirtyDotTracksTheDocument() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            StatusBar bar = new StatusBar();
            bar.bindTo(controller);

            assertFalse(bar.dirtyLabel().isVisible());
            controller.getDocument().setTitle("x");
            assertTrue(bar.dirtyLabel().isVisible());
            controller.getDocument().markSaved();
            assertFalse(bar.dirtyLabel().isVisible());
        });
    }

    @Test
    void taskCountFollowsTheListAndDocumentSwaps() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            StatusBar bar = new StatusBar();
            bar.bindTo(controller);

            assertEquals("0 tasks", bar.taskCountLabel().getText());
            controller.getDocument().addTask(new GanttTask("a", DAY, 1));
            assertEquals("1 task", bar.taskCountLabel().getText());
            controller.getDocument().addTask(new GanttTask("b", DAY, 1));
            assertEquals("2 tasks", bar.taskCountLabel().getText());

            controller.newDocument();
            assertEquals("0 tasks", bar.taskCountLabel().getText());
            controller.getDocument().addTask(new GanttTask("c", DAY, 1));
            assertEquals("1 task", bar.taskCountLabel().getText());
        });
    }

    @Test
    void zoomPercentFollowsTheZoomCommands() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            StatusBar bar = new StatusBar();
            bar.bindTo(controller);

            assertEquals("100%", bar.zoomLabel().getText());
            controller.zoomIn();
            assertEquals("125%", bar.zoomLabel().getText());
        });
    }

    @Test
    void fileNameAppearsAfterSaveAs(@TempDir Path dir) throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            Path target = dir.resolve("plan.wintak.json");
            controller.setFileDialogs(new FileDialogs() {
                @Override
                public Optional<Path> chooseOpen() {
                    return Optional.empty();
                }

                @Override
                public Optional<Path> chooseSaveTarget() {
                    return Optional.of(target);
                }
            });
            StatusBar bar = new StatusBar();
            bar.bindTo(controller);

            assertEquals(StatusBar.NO_FILE, bar.fileLabel().getText());
            controller.saveAs();
            assertEquals("plan.wintak.json", bar.fileLabel().getText());
        });
    }
}
