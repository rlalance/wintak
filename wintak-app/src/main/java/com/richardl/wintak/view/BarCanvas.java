package com.richardl.wintak.view;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

import java.util.List;

/**
 * The chart body: one horizontal bar per task, geometry entirely from {@link TimeScale} so
 * the math stays unit-tested. Rows align 1:1 with the task table's row order.
 */
public class BarCanvas extends Pane {

    public static final double ROW_HEIGHT = 28;
    public static final double BAR_HEIGHT = 18;
    public static final double BAR_TOP_INSET = (ROW_HEIGHT - BAR_HEIGHT) / 2;
    /** Horizontal lead-out before an arrow turns toward its successor's row. */
    public static final double ARROW_STUB = 8;
    /** Click within this many px of an arrow line counts as a hit. */
    public static final double ARROW_HIT_TOLERANCE = 4.0;

    public BarCanvas() {
        setId("bar-canvas");
        getStyleClass().add("bar-canvas");
    }

    /** Rebuilds all bars for the given scale and row-ordered task list. */
    public void render(TimeScale scale, List<GanttTask> tasks) {
        render(scale, tasks, List.of());
    }

    /** Rebuilds bars plus finish-to-start dependency elbows. */
    public void render(TimeScale scale, List<GanttTask> tasks, List<Dependency> dependencies) {
        getChildren().clear();
        int row = 0;
        for (GanttTask task : tasks) {
            getChildren().add(task.isMilestone() ? diamond(scale, task, row) : bar(scale, task, row));
            row++;
        }
        for (Dependency link : dependencies) {
            getChildren().add(elbow(scale, link, tasks));
        }
        setPrefHeight(tasks.size() * ROW_HEIGHT);
    }

    /**
     * Elbow from the predecessor's finish edge to the successor's start edge: out a short
     * stub, down/up to the successor's row, then in. Both endpoints sit on row centres.
     */
    private static Polyline elbow(TimeScale scale, Dependency link, List<GanttTask> tasks) {
        double fromX = link.predecessor().isMilestone()
                ? scale.xOf(link.predecessor().getStart()) + BAR_HEIGHT / 2.0
                : scale.xOf(link.predecessor().getStart()) + link.predecessor().getDurationDays() * scale.pxPerDay();
        double fromY = tasks.indexOf(link.predecessor()) * ROW_HEIGHT + ROW_HEIGHT / 2;
        double toX = link.successor().isMilestone()
                ? scale.xOf(link.successor().getStart()) - BAR_HEIGHT / 2.0
                : scale.xOf(link.successor().getStart());
        double toY = tasks.indexOf(link.successor()) * ROW_HEIGHT + ROW_HEIGHT / 2;
        double stubX = Math.min(fromX + ARROW_STUB, toX);

        Polyline line = new Polyline(fromX, fromY, stubX, fromY, stubX, toY, toX, toY);
        line.getStyleClass().add("dependency-arrow");
        line.setUserData(link);
        return line;
    }

    private static Region bar(TimeScale scale, GanttTask task, int row) {
        double width = task.getDurationDays() * scale.pxPerDay();

        Region fill = new Region();
        fill.getStyleClass().add("task-bar-progress");
        fill.setPrefSize(width * task.getPercentComplete() / 100.0, BAR_HEIGHT);
        fill.setMaxWidth(Region.USE_PREF_SIZE);

        Pane bar = new Pane(fill);
        bar.getStyleClass().add("task-bar");
        bar.setLayoutX(scale.xOf(task.getStart()));
        bar.setLayoutY(row * ROW_HEIGHT + BAR_TOP_INSET);
        bar.setPrefSize(width, BAR_HEIGHT);
        bar.setUserData(task);
        return bar;
    }

    private static Polygon diamond(TimeScale scale, GanttTask task, int row) {
        double cx = scale.xOf(task.getStart());
        double cy = row * ROW_HEIGHT + ROW_HEIGHT / 2.0;
        double r = BAR_HEIGHT / 2.0;
        Polygon d = new Polygon(cx, cy - r, cx + r, cy, cx, cy + r, cx - r, cy);
        d.getStyleClass().add("milestone-diamond");
        d.setUserData(task);
        return d;
    }

    /** The bar nodes in row order. */
    public List<Region> bars() {
        return getChildren().stream()
                .filter(n -> n.getStyleClass().contains("task-bar"))
                .map(Region.class::cast)
                .toList();
    }

    /** The milestone diamond nodes in row order. */
    public List<Polygon> diamonds() {
        return getChildren().stream()
                .filter(n -> n.getStyleClass().contains("milestone-diamond"))
                .map(Polygon.class::cast)
                .toList();
    }

    /** The dependency elbows currently drawn. */
    public List<Polyline> arrows() {
        return getChildren().stream()
                .filter(n -> n.getStyleClass().contains("dependency-arrow"))
                .map(Polyline.class::cast)
                .toList();
    }

    public Region progressFillOf(Region bar) {
        return (Region) ((Pane) bar).getChildren().stream()
                .filter(n -> n.getStyleClass().contains("task-bar-progress"))
                .findFirst()
                .orElseThrow();
    }

    /** The task a bar renders (used by the editing tools). */
    public GanttTask taskOf(Region bar) {
        return (GanttTask) bar.getUserData();
    }

    /** Geometric hit test against bars; returns null if nothing is hit. */
    public Region barAt(double x, double y) {
        for (Region bar : bars()) {
            if (x >= bar.getLayoutX() && x <= bar.getLayoutX() + bar.getPrefWidth()
                    && y >= bar.getLayoutY() && y <= bar.getLayoutY() + BAR_HEIGHT) {
                return bar;
            }
        }
        return null;
    }

    /** Returns the {@link Dependency} whose elbow polyline the point (x, y) is near, or null. */
    public Dependency arrowAt(double x, double y) {
        for (Polyline arrow : arrows()) {
            List<Double> pts = arrow.getPoints();
            for (int i = 0; i < pts.size() - 2; i += 2) {
                if (distToSegment(x, y, pts.get(i), pts.get(i + 1),
                        pts.get(i + 2), pts.get(i + 3)) <= ARROW_HIT_TOLERANCE) {
                    return (Dependency) arrow.getUserData();
                }
            }
        }
        return null;
    }

    private static double distToSegment(double px, double py,
                                        double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) return Math.hypot(px - ax, py - ay);
        double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    /** Hit test including milestone diamonds (L1 norm inside the rotated square). */
    public GanttTask taskAt(double x, double y) {
        Region bar = barAt(x, y);
        if (bar != null) return taskOf(bar);
        double r = BAR_HEIGHT / 2.0;
        for (Polygon d : diamonds()) {
            // points: [cx, cy-r, cx+r, cy, cx, cy+r, cx-r, cy]  (top/right/bottom/left)
            double cx = d.getPoints().get(0); // top-x == center-x
            double cy = d.getPoints().get(3); // right-y == center-y
            if (Math.abs(x - cx) + Math.abs(y - cy) <= r) {
                return (GanttTask) d.getUserData();
            }
        }
        return null;
    }
}
