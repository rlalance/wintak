package com.richardl.wintak.app;

import com.richardl.wintak.controller.UserPrompts;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Window;

/** Real blocking Alert prompts. Eye-verified; unit tests use stubs against the seam. */
public final class FxUserPrompts implements UserPrompts {

    private static final ButtonType SAVE = new ButtonType("Save", ButtonBar.ButtonData.YES);
    private static final ButtonType DISCARD = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);

    private final Window owner;

    public FxUserPrompts(Window owner) {
        this.owner = owner;
    }

    @Override
    public SaveChoice confirmUnsaved() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "The project has unsaved changes. Save them first?",
                SAVE, DISCARD, ButtonType.CANCEL);
        alert.setHeaderText("Unsaved changes");
        alert.initOwner(owner);
        ButtonType answer = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (answer == SAVE) {
            return SaveChoice.SAVE;
        }
        return answer == DISCARD ? SaveChoice.DISCARD : SaveChoice.CANCEL;
    }

    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    @Override
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("About " + WintakApp.APP_TITLE);
        alert.setHeaderText(WintakApp.APP_TITLE + " 1.0");
        Label content = new Label("A JavaFX Gantt-chart editor.\n\nhttps://github.com/richard-lam-dev/wintak");
        content.setWrapText(true);
        alert.getDialogPane().setContent(content);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}
