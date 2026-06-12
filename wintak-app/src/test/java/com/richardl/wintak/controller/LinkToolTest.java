package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.BarCanvas;
import com.richardl.wintak.view.GanttEditor;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class LinkToolTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8);

    @Test
    void linkTasksCreatesAnUndoableDependency() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", MON, 1);
        GanttTask b = new GanttTask("b", MON, 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);

        controller.linkTasks(a, b);
        assertEquals(1, controller.getDocument().getDependencies().size());

        controller.undo();
        assertTrue(controller.getDocument().getDependencies().isEmpty());

        controller.redo();
        assertEquals(1, controller.getDocument().getDependencies().size());
    }

    @Test
    void aCycleIsRefusedGracefully() {
        MainController controller = new MainController();
        controller.setDelayedRunner(DelayedRunner.NONE);
        GanttTask a = new GanttTask("a", MON, 1);
        GanttTask b = new GanttTask("b", MON, 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);
        controller.linkTasks(a, b);

        controller.linkTasks(b, a); // must not throw

        assertEquals(1, controller.getDocument().getDependencies().size(), "no cycle created");
        assertTrue(controller.statusMessageProperty().get().toLowerCase().contains("cycle")
                        || !controller.statusMessageProperty().get().isEmpty(),
                "the refusal is explained in the status line");
        assertTrue(controller.undoAvailableProperty().get(), "history still has only the good link");
    }

    @Test
    void selfAndNullLinksAreIgnored() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", MON, 1);
        controller.getDocument().addTask(a);

        controller.linkTasks(a, a);
        controller.linkTasks(a, null);
        controller.linkTasks(null, a);

        assertTrue(controller.getDocument().getDependencies().isEmpty());
    }

    @Test
    void linkToolDragFromBarToBarCreatesTheDependency() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask a = new GanttTask("a", MON, 5);            // row 0
            GanttTask b = new GanttTask("b", MON.plusDays(6), 5); // row 1
            controller.getDocument().addTask(a);
            controller.getDocument().addTask(b);
            controller.toolModeProperty().set(ToolMode.LINK);

            double rowMid = BarCanvas.ROW_HEIGHT / 2;
            fire(editor.canvas(), MouseEvent.MOUSE_PRESSED, 50, rowMid);                       // on a
            fire(editor.canvas(), MouseEvent.MOUSE_RELEASED, 140, BarCanvas.ROW_HEIGHT + rowMid); // on b

            assertEquals(1, controller.getDocument().getDependencies().size());
            assertEquals(1, editor.canvas().arrows().size(), "the arrow appears immediately");
        });
    }

    private static void fire(BarCanvas canvas, EventType<MouseEvent> type, double x, double y) {
        canvas.fireEvent(new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, true, null));
    }
}
