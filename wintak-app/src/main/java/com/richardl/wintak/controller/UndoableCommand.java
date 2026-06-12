package com.richardl.wintak.controller;

/** One reversible edit. {@code undo} must be the exact inverse of {@code execute}. */
public interface UndoableCommand {

    void execute();

    void undo();

    /** Short human name ("Add Task") for status lines and menus. */
    String name();
}
