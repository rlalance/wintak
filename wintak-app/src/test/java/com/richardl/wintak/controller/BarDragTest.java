package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class BarDragTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8);
    private static final TimeScale SCALE = new TimeScale(MON, 20);

    @Test
    void movingSnapsToWholeDays() {
        GanttTask task = new GanttTask("t", MON, 5);
        BarDragSession drag = new BarDragSession(task, SCALE, 50, BarDragSession.Mode.MOVE);

        drag.dragTo(50 + 47); // 2.35 days right
        assertEquals(MON.plusDays(2), task.getStart(), "snapped to the nearest day");

        drag.dragTo(50 - 30); // 1.5 days left of the press point
        assertEquals(MON.minusDays(2), task.getStart(), "rounding is symmetric");
    }

    @Test
    void resizingChangesDurationAndNeverGoesBelowOneDay() {
        GanttTask task = new GanttTask("t", MON, 5);
        BarDragSession drag = new BarDragSession(task, SCALE, 100, BarDragSession.Mode.RESIZE);

        drag.dragTo(100 + 40);
        assertEquals(7, task.getDurationDays());

        drag.dragTo(100 - 200);
        assertEquals(1, task.getDurationDays(), "clamped at one day");
    }

    @Test
    void finishingProducesOneUndoableCommandForTheWholeDrag() {
        GanttTask task = new GanttTask("t", MON, 5);
        UndoStack stack = new UndoStack();
        BarDragSession drag = new BarDragSession(task, SCALE, 0, BarDragSession.Mode.MOVE);

        drag.dragTo(20);
        drag.dragTo(60);
        Optional<UndoableCommand> command = drag.finish();
        assertTrue(command.isPresent());
        stack.execute(command.get());

        assertEquals(MON.plusDays(3), task.getStart());
        stack.undo();
        assertEquals(MON, task.getStart(), "one undo reverts the whole drag");
    }

    @Test
    void aDragThatEndsWhereItStartedIsNotAnEdit() {
        GanttTask task = new GanttTask("t", MON, 5);
        BarDragSession drag = new BarDragSession(task, SCALE, 0, BarDragSession.Mode.MOVE);
        drag.dragTo(8);  // less than half a day
        assertTrue(drag.finish().isEmpty());
    }

    @Test
    void draggingABarOnTheCanvasMovesItsTask() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask task = new GanttTask("t", MON, 5);
            controller.getDocument().addTask(task);

            double y = BarCanvas.ROW_HEIGHT / 2;
            fire(editor.canvas(), MouseEvent.MOUSE_PRESSED, 30, y);
            fire(editor.canvas(), MouseEvent.MOUSE_DRAGGED, 30 + 40, y);
            fire(editor.canvas(), MouseEvent.MOUSE_RELEASED, 30 + 40, y);

            assertEquals(MON.plusDays(2), task.getStart());
            controller.undo();
            assertEquals(MON, task.getStart());
        });
    }

    @Test
    void draggingTheRightEdgeResizes() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask task = new GanttTask("t", MON, 5); // bar 0..100
            controller.getDocument().addTask(task);

            double y = BarCanvas.ROW_HEIGHT / 2;
            fire(editor.canvas(), MouseEvent.MOUSE_PRESSED, 98, y);
            fire(editor.canvas(), MouseEvent.MOUSE_DRAGGED, 98 + 40, y);
            fire(editor.canvas(), MouseEvent.MOUSE_RELEASED, 98 + 40, y);

            assertEquals(7, task.getDurationDays());
            assertEquals(MON, task.getStart(), "resizing never moves the start");
        });
    }

    private static void fire(BarCanvas canvas, EventType<MouseEvent> type, double x, double y) {
        canvas.fireEvent(new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, true, null));
    }
}
