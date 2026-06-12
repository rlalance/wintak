package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.GanttEditor;
import com.richardl.wintak.view.MainMenuBar;
import com.richardl.wintak.view.RootLayout;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class SelectionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void deleteSelectedTaskIsUndoableAndClearsSelection() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", DAY, 1);
        controller.getDocument().addTask(task);
        controller.selectedTaskProperty().set(task);

        controller.deleteSelectedTask();
        assertTrue(controller.getDocument().getTasks().isEmpty());
        assertNull(controller.selectedTaskProperty().get());

        controller.undo();
        assertEquals(1, controller.getDocument().getTasks().size());
    }

    @Test
    void deleteWithNoSelectionIsANoOp() {
        MainController controller = new MainController();
        controller.getDocument().addTask(new GanttTask("t", DAY, 1));
        controller.deleteSelectedTask();
        assertEquals(1, controller.getDocument().getTasks().size());
    }

    @Test
    void selectToolClickOnABarSelectsItsTask() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask task = new GanttTask("t", DAY, 5);
            controller.getDocument().addTask(task);

            editor.canvas().bars().get(0).fireEvent(click());
            assertSame(task, controller.selectedTaskProperty().get());
        });
    }

    @Test
    void theSelectedBarIsHighlighted() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask a = new GanttTask("a", DAY, 1);
            GanttTask b = new GanttTask("b", DAY, 1);
            controller.getDocument().addTask(a);
            controller.getDocument().addTask(b);

            controller.selectedTaskProperty().set(b);
            assertFalse(editor.canvas().bars().get(0).getStyleClass().contains("selected"));
            assertTrue(editor.canvas().bars().get(1).getStyleClass().contains("selected"));
        });
    }

    @Test
    void tableSelectionStaysInSyncBothWays() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask a = new GanttTask("a", DAY, 1);
            GanttTask b = new GanttTask("b", DAY, 1);
            controller.getDocument().addTask(a);
            controller.getDocument().addTask(b);

            editor.table().getSelectionModel().select(b);
            assertSame(b, controller.selectedTaskProperty().get());

            controller.selectedTaskProperty().set(a);
            assertSame(a, editor.table().getSelectionModel().getSelectedItem());
        });
    }

    @Test
    void deleteKeyIsTheMenuAccelerator() throws Exception {
        FxThread.run(() -> {
            MainMenuBar menuBar = (MainMenuBar) new RootLayout(new MainController()).getTop();
            assertEquals(new javafx.scene.input.KeyCodeCombination(KeyCode.DELETE),
                    (KeyCombination) menuBar.item("menu-edit-delete-task").getAccelerator());
        });
    }

    private static MouseEvent click() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 1, 1, 1, 1, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, true, null);
    }
}
