package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoStackTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void executeRunsAndEnablesUndo() {
        UndoStack stack = new UndoStack();
        GanttDocument doc = new GanttDocument();
        GanttTask task = new GanttTask("t", DAY, 1);

        assertFalse(stack.canUndoProperty().get());
        stack.execute(new AddTaskCommand(doc, task));

        assertEquals(List.of(task), List.copyOf(doc.getTasks()));
        assertTrue(stack.canUndoProperty().get());
        assertFalse(stack.canRedoProperty().get());
    }

    @Test
    void undoRevertsAndRedoReapplies() {
        UndoStack stack = new UndoStack();
        GanttDocument doc = new GanttDocument();
        GanttTask task = new GanttTask("t", DAY, 1);
        stack.execute(new AddTaskCommand(doc, task));

        stack.undo();
        assertTrue(doc.getTasks().isEmpty());
        assertFalse(stack.canUndoProperty().get());
        assertTrue(stack.canRedoProperty().get());

        stack.redo();
        assertEquals(List.of(task), List.copyOf(doc.getTasks()));
        assertTrue(stack.canUndoProperty().get());
        assertFalse(stack.canRedoProperty().get());
    }

    @Test
    void aNewCommandClearsTheRedoBranch() {
        UndoStack stack = new UndoStack();
        GanttDocument doc = new GanttDocument();
        stack.execute(new AddTaskCommand(doc, new GanttTask("a", DAY, 1)));
        stack.undo();

        stack.execute(new AddTaskCommand(doc, new GanttTask("b", DAY, 1)));
        assertFalse(stack.canRedoProperty().get(), "history forked \u2014 the old future is gone");
    }

    @Test
    void removeTaskUndoRestoresPositionAndLinks() {
        UndoStack stack = new UndoStack();
        GanttDocument doc = new GanttDocument();
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY, 1);
        GanttTask c = new GanttTask("c", DAY, 1);
        doc.addTask(a);
        doc.addTask(b);
        doc.addTask(c);
        doc.addDependency(a, b);
        doc.addDependency(b, c);

        stack.execute(new RemoveTaskCommand(doc, b));
        assertEquals(List.of(a, c), List.copyOf(doc.getTasks()));
        assertTrue(doc.getDependencies().isEmpty());

        stack.undo();
        assertEquals(List.of(a, b, c), List.copyOf(doc.getTasks()), "b back in the middle");
        assertEquals(2, doc.getDependencies().size(), "both links restored");
    }

    @Test
    void clearForgetsEverything() {
        UndoStack stack = new UndoStack();
        GanttDocument doc = new GanttDocument();
        stack.execute(new AddTaskCommand(doc, new GanttTask("a", DAY, 1)));
        stack.undo();

        stack.clear();
        assertFalse(stack.canUndoProperty().get());
        assertFalse(stack.canRedoProperty().get());
    }

    @Test
    void controllerWiresTheStackAndDocumentSwapsClearIt() {
        MainController controller = new MainController();
        GanttTask task = new GanttTask("t", DAY, 1);

        controller.execute(new AddTaskCommand(controller.getDocument(), task));
        assertTrue(controller.undoAvailableProperty().get());

        controller.undo();
        assertTrue(controller.getDocument().getTasks().isEmpty());
        assertTrue(controller.redoAvailableProperty().get());

        controller.redo();
        assertEquals(1, controller.getDocument().getTasks().size());

        controller.newDocument();
        assertFalse(controller.undoAvailableProperty().get(), "a fresh document has no history");
        assertFalse(controller.redoAvailableProperty().get());
    }
}
