package com.richardl.wintak.controller;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttDocument;

/** Removes a finish-to-start link; undo re-adds it. */
public final class RemoveDependencyCommand implements UndoableCommand {

    private final GanttDocument document;
    private final Dependency link;

    public RemoveDependencyCommand(GanttDocument document, Dependency link) {
        this.document = document;
        this.link = link;
    }

    @Override
    public void execute() {
        document.removeDependency(link.predecessor(), link.successor());
    }

    @Override
    public void undo() {
        document.addDependency(link.predecessor(), link.successor());
    }

    @Override
    public String name() {
        return "Delete Link";
    }
}
