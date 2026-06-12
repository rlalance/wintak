package com.richardl.wintak.controller;

import java.nio.file.Path;
import java.util.Optional;

/** Seam around the platform file dialogs so commands are testable with stubs. */
public interface FileDialogs {

    /** Empty when the user cancels. */
    Optional<Path> chooseOpen();

    /** Empty when the user cancels. */
    Optional<Path> chooseSaveTarget();

    /** Null object: behaves as if the user always cancels. */
    FileDialogs NONE = new FileDialogs() {
        @Override
        public Optional<Path> chooseOpen() {
            return Optional.empty();
        }

        @Override
        public Optional<Path> chooseSaveTarget() {
            return Optional.empty();
        }
    };
}
