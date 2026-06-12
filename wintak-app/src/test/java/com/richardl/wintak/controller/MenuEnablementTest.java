package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.MainMenuBar;
import com.richardl.wintak.view.RootLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class MenuEnablementTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void saveAvailableTracksDirtyAcrossDocumentSwaps() {
        MainController controller = new MainController();
        assertFalse(controller.saveAvailableProperty().get());

        controller.getDocument().addTask(new GanttTask("t", DAY, 1));
        assertTrue(controller.saveAvailableProperty().get());

        controller.setUserPrompts(UserPrompts.NONE);
        controller.newDocument();
        assertFalse(controller.saveAvailableProperty().get(), "fresh document is clean again");

        controller.getDocument().setTitle("x");
        assertTrue(controller.saveAvailableProperty().get(), "tracking follows the new document");
    }

    @Test
    void undoRedoAndDeleteStartUnavailable() {
        MainController controller = new MainController();
        assertFalse(controller.undoAvailableProperty().get());
        assertFalse(controller.redoAvailableProperty().get());
        assertFalse(controller.deleteAvailableProperty().get());
    }

    @Test
    void deleteBecomesAvailableWithASelection() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", DAY, 1);
        controller.getDocument().addTask(task);
        controller.selectedTaskProperty().set(task);
        assertTrue(controller.deleteAvailableProperty().get());
    }

    @Test
    void menuItemsAreBoundToTheControllerState() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            MainMenuBar menuBar = (MainMenuBar) root.getTop();

            assertTrue(menuBar.item("menu-file-save").isDisable(), "clean: Save disabled");
            assertTrue(menuBar.item("menu-edit-undo").isDisable());
            assertTrue(menuBar.item("menu-edit-redo").isDisable());
            assertTrue(menuBar.item("menu-edit-delete-task").isDisable());

            GanttTask task = new GanttTask("t", DAY, 1);
            controller.getDocument().addTask(task);
            assertFalse(menuBar.item("menu-file-save").isDisable(), "dirty: Save enabled");

            controller.selectedTaskProperty().set(task);
            assertFalse(menuBar.item("menu-edit-delete-task").isDisable());
        });
    }
}
