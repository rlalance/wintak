package com.richardl.wintak.view;

import com.richardl.wintak.controller.BarDragSession;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.controller.ToolMode;
import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The document editor: task table on the left, timeline (ruler + bar canvas in a scroll
 * pane) on the right. Re-renders on any model, dependency, zoom or document change; owns
 * the viewport behaviours (Ctrl+wheel zoom, shared vertical scroll, zoom-to-fit).
 */
public class GanttEditor extends SplitPane {

    /** Breathing room rendered after the last task's end. */
    public static final int TRAILING_DAYS = 7;

    private final MainController controller;
    private final TaskTable table;
    private final TimelineHeader header = new TimelineHeader();
    private final BarCanvas canvas = new BarCanvas();
    private final ScrollPane scroll = new ScrollPane(canvas);

    /** Grabbing within this many px of a bar's right edge resizes instead of moving. */
    public static final double RESIZE_HANDLE_PX = 6;

    private final InvalidationListener onAnyChange = obs -> refresh();
    private final List<GanttTask> subscribed = new ArrayList<>();
    private TimeScale currentScale;
    private BarDragSession drag;
    private GanttTask linkSource;

    public GanttEditor(MainController controller) {
        this.controller = controller;
        setId("gantt-editor");
        table = new TaskTable(controller);

        scroll.setFitToHeight(true);
        VBox timeline = new VBox(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getItems().addAll(table, timeline);
        setDividerPositions(0.35);

        hook(controller.getDocument());
        controller.documentProperty().addListener((obs, old, next) -> {
            unhook(old);
            hook(next);
            refresh();
        });
        controller.zoomPxPerDayProperty().addListener(onAnyChange);
        controller.setOnZoomToFit(this::zoomToFit);

        // Ctrl+wheel zooms the timeline; plain wheel keeps scrolling.
        scroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) {
                    controller.zoomIn();
                } else if (e.getDeltaY() < 0) {
                    controller.zoomOut();
                }
                e.consume();
            }
        });

        // Tool clicks: ADD_TASK creates at the clicked date, SELECT picks the clicked task
        // (bars and milestone diamonds), or clears the selection on empty canvas.
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            switch (controller.toolModeProperty().get()) {
                case ADD_TASK -> controller.addTaskAt(currentScale.dateAt(e.getX()));
                case ADD_MILESTONE -> controller.addMilestoneAt(currentScale.dateAt(e.getX()));
                case SELECT -> {
                    // Node-walk finds bars (and their progress-fill children) regardless of
                    // coordinate origin; taskAt() catches milestone diamonds via geometry.
                    GanttTask hit = barTaskOf(e.getTarget());
                    if (hit == null) hit = canvas.taskAt(e.getX(), e.getY());
                    if (hit != null) {
                        controller.selectedTaskProperty().set(hit);
                    } else {
                        Dependency arrow = canvas.arrowAt(e.getX(), e.getY());
                        if (arrow != null) {
                            controller.selectedDependencyProperty().set(arrow);
                        } else {
                            controller.selectedTaskProperty().set(null);
                            controller.selectedDependencyProperty().set(null);
                        }
                    }
                }
                default -> { }
            }
        });

        // SELECT drags move/resize a bar (milestones are not draggable - barAt misses them
        // intentionally); LINK drags use taskAt() so both bars and diamonds can be endpoints.
        canvas.setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }
            switch (controller.toolModeProperty().get()) {
                case SELECT -> {
                    Region bar = canvas.barAt(e.getX(), e.getY());
                    if (bar == null) {
                        return;
                    }
                    GanttTask task = canvas.taskOf(bar);
                    controller.selectedTaskProperty().set(task);
                    boolean onRightEdge =
                            e.getX() >= bar.getLayoutX() + bar.getPrefWidth() - RESIZE_HANDLE_PX;
                    drag = new BarDragSession(task, currentScale, e.getX(),
                            onRightEdge ? BarDragSession.Mode.RESIZE : BarDragSession.Mode.MOVE);
                }
                case LINK -> {
                    GanttTask task = canvas.taskAt(e.getX(), e.getY());
                    if (task != null) {
                        linkSource = task;
                    }
                }
                default -> { }
            }
        });
        canvas.setOnMouseDragged(e -> {
            if (drag != null) {
                drag.dragTo(e.getX());
            }
        });
        canvas.setOnMouseReleased(e -> {
            if (drag != null) {
                drag.finish().ifPresent(controller::execute);
                drag = null;
            }
            if (linkSource != null) {
                GanttTask target = canvas.taskAt(e.getX(), e.getY());
                if (target != null) {
                    controller.linkTasks(linkSource, target);
                }
                linkSource = null;
            }
        });

        // Selection flows both ways between table, chart highlight and controller.
        controller.selectedTaskProperty().addListener(onAnyChange);
        controller.selectedDependencyProperty().addListener(onAnyChange);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, next) -> {
            if (next != null) {
                controller.selectedTaskProperty().set(next);
            }
        });
        controller.selectedTaskProperty().addListener((obs, old, next) -> {
            if (next == null) {
                table.getSelectionModel().clearSelection();
            } else if (table.getSelectionModel().getSelectedItem() != next) {
                table.getSelectionModel().select(next);
            }
        });

        // Canvas scroll drives the table so rows stay aligned.
        scroll.vvalueProperty().addListener((obs, old, next) -> {
            double scrollable = Math.max(0, canvas.getPrefHeight() - scroll.getHeight());
            int topRow = (int) (next.doubleValue() * scrollable / BarCanvas.ROW_HEIGHT);
            table.scrollTo(Math.max(0, topRow));
        });

        refresh();
    }

    private void hook(GanttDocument doc) {
        doc.getTasks().addListener(onAnyChange);
        doc.getDependencies().addListener(onAnyChange);
        resubscribeTasks(doc);
    }

    private void unhook(GanttDocument doc) {
        doc.getTasks().removeListener(onAnyChange);
        doc.getDependencies().removeListener(onAnyChange);
    }

    /** Bars must follow per-task edits, so every task's properties feed the refresh. */
    private void resubscribeTasks(GanttDocument doc) {
        for (GanttTask task : subscribed) {
            task.nameProperty().removeListener(onAnyChange);
            task.startProperty().removeListener(onAnyChange);
            task.durationDaysProperty().removeListener(onAnyChange);
            task.percentCompleteProperty().removeListener(onAnyChange);
        }
        subscribed.clear();
        for (GanttTask task : doc.getTasks()) {
            task.nameProperty().addListener(onAnyChange);
            task.startProperty().addListener(onAnyChange);
            task.durationDaysProperty().addListener(onAnyChange);
            task.percentCompleteProperty().addListener(onAnyChange);
            subscribed.add(task);
        }
    }

    private void refresh() {
        GanttDocument doc = controller.getDocument();
        resubscribeTasks(doc);

        LocalDate origin = doc.getTasks().stream()
                .map(GanttTask::getStart)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate end = doc.getTasks().stream()
                .map(GanttTask::getEndExclusive)
                .max(LocalDate::compareTo)
                .orElse(origin)
                .plusDays(TRAILING_DAYS);

        TimeScale scale = new TimeScale(origin, controller.zoomPxPerDayProperty().get());
        currentScale = scale;
        header.render(scale, origin, end);
        canvas.render(scale, doc.getTasks(), doc.getDependencies());
        canvas.setPrefWidth(scale.xOf(end));

        GanttTask selected = controller.selectedTaskProperty().get();
        for (Region bar : canvas.bars()) {
            if (canvas.taskOf(bar) == selected) {
                bar.getStyleClass().add("selected");
            }
        }
        for (Polygon diamond : canvas.diamonds()) {
            if ((GanttTask) diamond.getUserData() == selected) {
                diamond.getStyleClass().add("selected");
            }
        }
        Dependency selectedLink = controller.selectedDependencyProperty().get();
        for (Polyline arrow : canvas.arrows()) {
            if (arrow.getUserData() == selectedLink && selectedLink != null) {
                arrow.getStyleClass().add("selected");
            }
        }
    }

    /** Walks up from an event target to the enclosing task bar, if any. */
    private GanttTask barTaskOf(Object target) {
        Node node = target instanceof Node n ? n : null;
        while (node != null && node != canvas) {
            if (node.getStyleClass().contains("task-bar")) {
                return canvas.taskOf((Region) node);
            }
            node = node.getParent();
        }
        return null;
    }

    /** px/day that makes the whole document span fill the viewport, clamped to zoom bounds. */
    public static double fitPxPerDay(double viewportWidth, long spanDays) {
        double fit = viewportWidth / Math.max(1, spanDays);
        return Math.max(MainController.MIN_PX_PER_DAY, Math.min(MainController.MAX_PX_PER_DAY, fit));
    }

    private void zoomToFit() {
        GanttDocument doc = controller.getDocument();
        if (doc.getTasks().isEmpty()) {
            return;
        }
        LocalDate origin = doc.getTasks().stream()
                .map(GanttTask::getStart).min(LocalDate::compareTo).orElseThrow();
        LocalDate end = doc.getTasks().stream()
                .map(GanttTask::getEndExclusive).max(LocalDate::compareTo).orElseThrow()
                .plusDays(TRAILING_DAYS);
        double viewport = scroll.getViewportBounds().getWidth();
        if (viewport <= 0) {
            viewport = getWidth() > 0 ? getWidth() * 0.65 : 800;
        }
        controller.zoomPxPerDayProperty().set(
                fitPxPerDay(viewport, java.time.temporal.ChronoUnit.DAYS.between(origin, end)));
        scroll.setHvalue(0);
    }

    public TaskTable table() {
        return table;
    }

    public TimelineHeader header() {
        return header;
    }

    public BarCanvas canvas() {
        return canvas;
    }
}
