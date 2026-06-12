package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.controller.RenameTaskCommand;
import com.richardl.wintak.model.GanttTask;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.time.LocalDate;

/**
 * Left pane of the Gantt editor: one row per task, kept in sync with the current document
 * (including document swaps). Start and duration render in the mono type role.
 */
public class TaskTable extends TableView<GanttTask> {

    public TaskTable(MainController controller) {
        setId("task-table");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setEditable(true);

        TableColumn<GanttTask, String> name = new TableColumn<>("Task");
        name.setId("col-name");
        name.setCellValueFactory(row -> row.getValue().nameProperty());
        name.setCellFactory(TextFieldTableCell.forTableColumn());
        name.setOnEditCommit(edit -> {
            // inline rename, routed through the undo history; blank input is ignored
            if (edit.getNewValue() != null && !edit.getNewValue().isBlank()) {
                controller.execute(new RenameTaskCommand(edit.getRowValue(), edit.getNewValue()));
            }
        });

        TableColumn<GanttTask, LocalDate> start = new TableColumn<>("Start");
        start.setId("col-start");
        start.setCellValueFactory(row -> row.getValue().startProperty());
        start.getStyleClass().add("wintak-mono");

        TableColumn<GanttTask, Integer> duration = new TableColumn<>("Days");
        duration.setId("col-duration");
        duration.setCellValueFactory(row -> row.getValue().durationDaysProperty().asObject());
        duration.getStyleClass().add("wintak-mono");

        TableColumn<GanttTask, Integer> progress = new TableColumn<>("%");
        progress.setId("col-progress");
        progress.setEditable(true);
        progress.setCellValueFactory(row -> row.getValue().percentCompleteProperty().asObject());
        progress.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        progress.setOnEditCommit(edit -> {
            if (edit.getNewValue() != null) {
                controller.setTaskProgress(edit.getRowValue(), edit.getNewValue());
            }
        });
        progress.getStyleClass().add("wintak-mono");

        getColumns().add(name);
        getColumns().add(start);
        getColumns().add(duration);
        getColumns().add(progress);

        setItems(controller.getDocument().getTasks());
        controller.documentProperty().addListener((obs, old, next) -> setItems(next.getTasks()));
    }
}
