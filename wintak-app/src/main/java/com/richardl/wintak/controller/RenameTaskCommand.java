package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;

/** Renames a task; undo restores the previous name. */
public final class RenameTaskCommand implements UndoableCommand {

    private final GanttTask task;
    private final String newName;
    private String oldName;

    public RenameTaskCommand(GanttTask task, String newName) {
        this.task = task;
        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = task.getName();
        task.setName(newName);
    }

    @Override
    public void undo() {
        task.setName(oldName);
    }

    @Override
    public String name() {
        return "Rename Task";
    }
}
