package com.richardl.wintak.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.Deque;

/** Linear undo/redo history; executing a new command discards the redo branch. */
public class UndoStack {

    private final Deque<UndoableCommand> undo = new ArrayDeque<>();
    private final Deque<UndoableCommand> redo = new ArrayDeque<>();
    private final BooleanProperty canUndo = new SimpleBooleanProperty(this, "canUndo", false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(this, "canRedo", false);

    public void execute(UndoableCommand command) {
        command.execute();
        undo.push(command);
        redo.clear();
        sync();
    }

    public void undo() {
        if (!undo.isEmpty()) {
            UndoableCommand command = undo.pop();
            command.undo();
            redo.push(command);
            sync();
        }
    }

    public void redo() {
        if (!redo.isEmpty()) {
            UndoableCommand command = redo.pop();
            command.execute();
            undo.push(command);
            sync();
        }
    }

    /** Forget all history (document replaced - a load must not be undoable). */
    public void clear() {
        undo.clear();
        redo.clear();
        sync();
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo;
    }

    private void sync() {
        canUndo.set(!undo.isEmpty());
        canRedo.set(!redo.isEmpty());
    }
}
