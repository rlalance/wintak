package com.richardl.wintak.controller;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyDeletionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void selectedDependencyClearsSelectedTask() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY.plusDays(1), 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);
        controller.getDocument().addDependency(a, b);

        controller.selectedTaskProperty().set(a);
        controller.selectedDependencyProperty().set(new Dependency(a, b));

        assertNull(controller.selectedTaskProperty().get(),
                "setting selectedDependency must clear selectedTask");
    }

    @Test
    void selectedTaskClearsSelectedDependency() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY.plusDays(1), 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);
        controller.getDocument().addDependency(a, b);

        controller.selectedDependencyProperty().set(new Dependency(a, b));
        controller.selectedTaskProperty().set(a);

        assertNull(controller.selectedDependencyProperty().get(),
                "setting selectedTask must clear selectedDependency");
    }

    @Test
    void deleteAvailableWhenDependencySelected() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY.plusDays(1), 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);
        controller.getDocument().addDependency(a, b);

        assertFalse(controller.deleteAvailableProperty().get());
        controller.selectedDependencyProperty().set(new Dependency(a, b));
        assertTrue(controller.deleteAvailableProperty().get(),
                "deleteAvailable must be true when a dependency is selected");
    }

    @Test
    void selectedDependencyIsClearedOnDocumentSwap() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY.plusDays(1), 1);
        controller.getDocument().addTask(a);
        controller.getDocument().addTask(b);
        controller.getDocument().addDependency(a, b);
        controller.selectedDependencyProperty().set(new Dependency(a, b));

        controller.setUserPrompts(com.richardl.wintak.controller.UserPrompts.NONE);
        controller.newDocument();

        assertNull(controller.selectedDependencyProperty().get(),
                "selectedDependency must be cleared when the document is replaced");
        assertFalse(controller.deleteAvailableProperty().get(),
                "deleteAvailable must be false after document swap");
    }

    @Test
    void deleteSelectedTaskAlsoDeletesSelectedDependencyWhenNoTaskSelected() {
        MainController controller = new MainController();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY.plusDays(1), 1);
        GanttDocument doc = controller.getDocument();
        doc.addTask(a);
        doc.addTask(b);
        doc.addDependency(a, b);

        controller.selectedDependencyProperty().set(new Dependency(a, b));
        controller.deleteSelectedTask();

        assertTrue(doc.getDependencies().isEmpty(), "the dependency must be removed");
        assertNull(controller.selectedDependencyProperty().get());

        controller.undo();
        assertEquals(1, doc.getDependencies().size(), "undo must restore the dependency");
    }
}
