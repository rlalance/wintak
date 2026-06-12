package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Bottom status strip: transient message (grows), then file | dirty dot | task count | zoom.
 * Pure structure; live bindings arrive with {@code bindTo} (FEAT-18). Numbers render in the
 * mono type role per the design system.
 */
public class StatusBar extends HBox {

    public static final String NO_FILE = "(no file)";

    private final Label message = segment("status-message", "");
    private final Label file = segment("status-file", NO_FILE);
    private final Label dirty = segment("status-dirty", "\u25cf"); // black circle
    private final Label taskCount = segment("status-task-count", "0 tasks");
    private final Label zoom = segment("status-zoom", "100%");

    public StatusBar() {
        setId(RootLayout.STATUS_AREA);
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        setPadding(new Insets(3, 10, 3, 10));

        dirty.setVisible(false);
        taskCount.getStyleClass().add("wintak-mono");
        zoom.getStyleClass().add("wintak-mono");

        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        getChildren().addAll(message, spring,
                file, dirty, new Separator(javafx.geometry.Orientation.VERTICAL),
                taskCount, new Separator(javafx.geometry.Orientation.VERTICAL), zoom);
    }

    /** Binds every segment to live controller state (FEAT-18). */
    public void bindTo(MainController controller) {
        message.textProperty().bind(controller.statusMessageProperty());
        dirty.visibleProperty().bind(controller.saveAvailableProperty());
        file.textProperty().bind(Bindings.createStringBinding(
                () -> controller.getCurrentFile() == null
                        ? NO_FILE
                        : controller.getCurrentFile().getFileName().toString(),
                controller.currentFileProperty()));
        taskCount.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    int n = controller.taskCountProperty().get();
                    return n + (n == 1 ? " task" : " tasks");
                },
                controller.taskCountProperty()));
        zoom.textProperty().bind(Bindings.createStringBinding(
                () -> Math.round(controller.zoomPxPerDayProperty().get()
                        / MainController.DEFAULT_PX_PER_DAY * 100) + "%",
                controller.zoomPxPerDayProperty()));
    }

    private static Label segment(String id, String initial) {
        Label label = new Label(initial);
        label.setId(id);
        return label;
    }

    public Label messageLabel() {
        return message;
    }

    public Label fileLabel() {
        return file;
    }

    public Label dirtyLabel() {
        return dirty;
    }

    public Label taskCountLabel() {
        return taskCount;
    }

    public Label zoomLabel() {
        return zoom;
    }
}
