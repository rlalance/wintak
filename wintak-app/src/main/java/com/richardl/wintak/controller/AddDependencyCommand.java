package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;

/** Creates a finish-to-start link; undo removes it. Validation lives in the document. */
public final class AddDependencyCommand implements UndoableCommand {

    private final GanttDocument document;
    private final GanttTask predecessor;
    private final GanttTask successor;

    public AddDependencyCommand(GanttDocument document, GanttTask predecessor, GanttTask successor) {
        this.document = document;
        this.predecessor = predecessor;
        this.successor = successor;
    }

    @Override
    public void execute() {
        document.addDependency(predecessor, successor);
    }

    @Override
    public void undo() {
        document.removeDependency(predecessor, successor);
    }

    @Override
    public String name() {
        return "Link Tasks";
    }
}
