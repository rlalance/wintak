package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.GanttEditor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class AddTaskTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void addTaskAtCreatesSelectsAndIsUndoable() {
        MainController controller = new MainController();

        controller.addTaskAt(DAY);

        assertEquals(1, controller.getDocument().getTasks().size());
        GanttTask created = controller.getDocument().getTasks().get(0);
        assertEquals(DAY, created.getStart());
        assertSame(created, controller.selectedTaskProperty().get(), "new task becomes the selection");

        controller.undo();
        assertTrue(controller.getDocument().getTasks().isEmpty());
    }

    @Test
    void menuAddTaskAppendsAfterTheProjectEnd() {
        MainController controller = new MainController();
        controller.getDocument().addTask(new GanttTask("existing", DAY, 5));

        controller.addTask();

        assertEquals(2, controller.getDocument().getTasks().size());
        assertEquals(DAY.plusDays(5), controller.getDocument().getTasks().get(1).getStart(),
                "appends where the project currently ends");
    }

    @Test
    void addTaskToolClickOnTheCanvasCreatesATaskAtThatDate() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            controller.getDocument().addTask(new GanttTask("anchor", DAY, 1));
            controller.toolModeProperty().set(ToolMode.ADD_TASK);

            // 3 day-cells right of the origin at the default 20 px/day
            double x = 3 * MainController.DEFAULT_PX_PER_DAY + 5;
            editor.canvas().fireEvent(click(x, 50));

            assertEquals(2, controller.getDocument().getTasks().size());
            assertEquals(DAY.plusDays(3), controller.getDocument().getTasks().get(1).getStart());
        });
    }

    @Test
    void selectToolClicksDoNotCreateTasks() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            controller.getDocument().addTask(new GanttTask("anchor", DAY, 1));

            editor.canvas().fireEvent(click(100, 50));
            assertEquals(1, controller.getDocument().getTasks().size());
        });
    }

    @Test
    void renameCommandIsUndoable() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("old name", DAY, 1);
        controller.getDocument().addTask(task);

        controller.execute(new RenameTaskCommand(task, "new name"));
        assertEquals("new name", task.getName());

        controller.undo();
        assertEquals("old name", task.getName());
    }

    private static MouseEvent click(double x, double y) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, true, null);
    }
}
