package com.richardl.wintak.app;

import com.richardl.wintak.controller.FileDialogs;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/** Real platform file dialogs. Eye-verified; unit tests use stubs against the seam. */
public final class FxFileDialogs implements FileDialogs {

    private final Window owner;

    public FxFileDialogs(Window owner) {
        this.owner = owner;
    }

    @Override
    public Optional<Path> chooseOpen() {
        return Optional.ofNullable(chooser("Open Project").showOpenDialog(owner)).map(File::toPath);
    }

    @Override
    public Optional<Path> chooseSaveTarget() {
        return Optional.ofNullable(chooser("Save Project").showSaveDialog(owner)).map(File::toPath);
    }

    private static FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wintak project (*.wintak.json)", "*.wintak.json"),
                new FileChooser.ExtensionFilter("JSON (*.json)", "*.json"),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
        return chooser;
    }
}
