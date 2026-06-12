package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;

/** Appends a task to the document; undo removes it again. */
public final class AddTaskCommand implements UndoableCommand {

    private final GanttDocument document;
    private final GanttTask task;

    public AddTaskCommand(GanttDocument document, GanttTask task) {
        this.document = document;
        this.task = task;
    }

    @Override
    public void execute() {
        document.addTask(task);
    }

    @Override
    public void undo() {
        document.removeTask(task);
    }

    @Override
    public String name() {
        return "Add Task";
    }
}
