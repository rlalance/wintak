package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FxToolkitExtension.class)
class TaskTableTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void hasNameStartAndDurationColumns() throws Exception {
        FxThread.run(() -> {
            TaskTable table = new TaskTable(new MainController());
            assertEquals(List.of("col-name", "col-start", "col-duration", "col-progress"),
                    table.getColumns().stream().map(c -> c.getId()).toList());
        });
    }

    @Test
    void rowsMirrorTheDocumentTasks() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            TaskTable table = new TaskTable(controller);

            GanttTask a = new GanttTask("Design", DAY, 5);
            controller.getDocument().addTask(a);
            assertEquals(List.of(a), List.copyOf(table.getItems()));

            GanttTask b = new GanttTask("Build", DAY.plusDays(5), 10);
            controller.getDocument().addTask(b);
            assertEquals(List.of(a, b), List.copyOf(table.getItems()));

            controller.getDocument().removeTask(a);
            assertEquals(List.of(b), List.copyOf(table.getItems()));
        });
    }

    @Test
    void rowsFollowADocumentSwap() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            TaskTable table = new TaskTable(controller);
            controller.getDocument().addTask(new GanttTask("old", DAY, 1));

            controller.newDocument();
            assertEquals(0, table.getItems().size());

            GanttTask fresh = new GanttTask("fresh", DAY, 2);
            controller.getDocument().addTask(fresh);
            assertEquals(List.of(fresh), List.copyOf(table.getItems()));
        });
    }

    @Test
    void columnsRenderTheTaskFields() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            TaskTable table = new TaskTable(controller);
            GanttTask task = new GanttTask("Design", DAY, 5);
            controller.getDocument().addTask(task);

            assertEquals("Design", table.getColumns().get(0).getCellData(0));
            assertEquals(DAY, table.getColumns().get(1).getCellData(0));
            assertEquals(5, table.getColumns().get(2).getCellData(0));
        });
    }
}
