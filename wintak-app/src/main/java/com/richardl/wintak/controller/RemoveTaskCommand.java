package com.richardl.wintak.controller;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;

import java.util.List;

/**
 * Removes a task; undo restores it at its original row and re-creates the dependency links
 * that were severed with it.
 */
public final class RemoveTaskCommand implements UndoableCommand {

    private final GanttDocument document;
    private final GanttTask task;
    private int index;
    private List<Dependency> severedLinks = List.of();

    public RemoveTaskCommand(GanttDocument document, GanttTask task) {
        this.document = document;
        this.task = task;
    }

    @Override
    public void execute() {
        index = document.getTasks().indexOf(task);
        severedLinks = document.getDependencies().stream()
                .filter(d -> d.predecessor() == task || d.successor() == task)
                .toList();
        document.removeTask(task);
    }

    @Override
    public void undo() {
        document.addTask(task);
        document.moveTask(document.getTasks().indexOf(task), index);
        for (Dependency link : severedLinks) {
            document.addDependency(link.predecessor(), link.successor());
        }
    }

    @Override
    public String name() {
        return "Delete Task";
    }
}
