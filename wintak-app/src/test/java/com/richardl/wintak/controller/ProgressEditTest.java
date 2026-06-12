package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.TaskTable;
import javafx.scene.control.TableColumn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FxToolkitExtension.class)
class ProgressEditTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8);

    @Test
    void setTaskProgressIsUndoable() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", MON, 5);
        controller.getDocument().addTask(task);

        controller.setTaskProgress(task, 60);
        assertEquals(60, task.getPercentComplete());

        controller.undo();
        assertEquals(0, task.getPercentComplete());

        controller.redo();
        assertEquals(60, task.getPercentComplete());
    }

    @Test
    void progressInputIsClampedTo0To100() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", MON, 5);
        controller.getDocument().addTask(task);

        controller.setTaskProgress(task, 250);
        assertEquals(100, task.getPercentComplete());

        controller.setTaskProgress(task, -10);
        assertEquals(0, task.getPercentComplete());
    }

    @Test
    void anUnchangedValueDoesNotPolluteTheHistory() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", MON, 5);
        controller.getDocument().addTask(task);
        controller.setTaskProgress(task, 0);

        assertEquals(false, controller.undoAvailableProperty().get(),
                "setting the same value is not an edit");
    }

    @Test
    void theTableHasAnEditableProgressColumn() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            TaskTable table = new TaskTable(controller);
            GanttTask task = new GanttTask("t", MON, 5);
            task.setPercentComplete(35);
            controller.getDocument().addTask(task);

            TableColumn<GanttTask, ?> progress = table.getColumns().stream()
                    .filter(c -> "col-progress".equals(c.getId()))
                    .findFirst().orElseThrow(() -> new AssertionError("no progress column"));
            assertEquals(true, progress.isEditable());
            assertEquals(35, progress.getCellData(0), "shows the task's percent-complete");
        });
    }
}
