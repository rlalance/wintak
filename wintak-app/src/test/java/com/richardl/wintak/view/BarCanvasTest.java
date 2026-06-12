package com.richardl.wintak.view;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import javafx.scene.shape.Polygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class BarCanvasTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8);

    @Test
    void oneBarPerTaskPositionedByTheTimeScale() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask design = new GanttTask("Design", MON, 5);
            GanttTask build = new GanttTask("Build", MON.plusDays(5), 10);

            canvas.render(scale, List.of(design, build));

            List<Region> bars = canvas.bars();
            assertEquals(2, bars.size());

            Region designBar = bars.get(0);
            assertEquals(0, designBar.getLayoutX());
            assertEquals(5 * 20, designBar.getPrefWidth());
            assertEquals(BarCanvas.BAR_TOP_INSET, designBar.getLayoutY());

            Region buildBar = bars.get(1);
            assertEquals(5 * 20, buildBar.getLayoutX());
            assertEquals(10 * 20, buildBar.getPrefWidth());
            assertEquals(BarCanvas.ROW_HEIGHT + BarCanvas.BAR_TOP_INSET, buildBar.getLayoutY());
        });
    }

    @Test
    void progressFillIsProportionalToPercentComplete() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            GanttTask task = new GanttTask("t", MON, 10);
            task.setPercentComplete(40);

            canvas.render(new TimeScale(MON, 10), List.of(task));

            Region fill = canvas.progressFillOf(canvas.bars().get(0));
            assertEquals(100 * 0.40, fill.getPrefWidth(), 1e-9, "40% of a 100px bar");
        });
    }

    @Test
    void milestoneRendersAsDiamondNotBar() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask ms = new GanttTask("Gate", MON, 1);
            ms.setMilestone(true);
            GanttTask task = new GanttTask("Task", MON.plusDays(2), 3);

            canvas.render(scale, List.of(ms, task));

            assertEquals(1, canvas.bars().size(), "only the non-milestone must be a task-bar");
            assertEquals(1, canvas.diamonds().size(), "milestone must produce a diamond");

            Polygon diamond = canvas.diamonds().get(0);
            assertTrue(diamond.getStyleClass().contains("milestone-diamond"));
            assertEquals(ms, diamond.getUserData(), "diamond must reference its task");
        });
    }

    @Test
    void taskAtHitTestFindsBarAndDiamond() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask ms = new GanttTask("Gate", MON, 1);
            ms.setMilestone(true);
            GanttTask task = new GanttTask("Task", MON.plusDays(5), 5);

            canvas.render(scale, List.of(ms, task));

            double cx = scale.xOf(MON);
            double cy = BarCanvas.ROW_HEIGHT / 2.0;
            assertEquals(ms, canvas.taskAt(cx, cy), "taskAt must find the diamond at its center");

            double barX = scale.xOf(MON.plusDays(5)) + 1;
            double barY = BarCanvas.ROW_HEIGHT + BarCanvas.BAR_TOP_INSET + 1;
            assertEquals(task, canvas.taskAt(barX, barY), "taskAt must find a regular bar");

            assertEquals(null, canvas.taskAt(-999, -999), "taskAt must return null on a miss");
        });
    }

    @Test
    void arrowAtReturnsDependencyOnHit() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask build = new GanttTask("Build", MON, 5);
            GanttTask gate = new GanttTask("Gate", MON.plusDays(10), 5);
            Dependency link = new Dependency(build, gate);

            canvas.render(scale, List.of(build, gate), List.of(link));

            // Elbow: fromX=100 fromY=14 stubX=108 toY=42 toX=200
            // Midpoint of the horizontal entry segment: (154, 42)
            Dependency hit = canvas.arrowAt(154, 42);
            assertEquals(link, hit, "arrowAt must return the dependency when clicking on the arrow");
        });
    }

    @Test
    void arrowAtReturnsNullOnMiss() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask build = new GanttTask("Build", MON, 5);
            GanttTask gate = new GanttTask("Gate", MON.plusDays(10), 5);

            canvas.render(scale, List.of(build, gate), List.of(new Dependency(build, gate)));

            assertNull(canvas.arrowAt(999, 999), "arrowAt must return null when nothing is near");
        });
    }

    @Test
    void renderIsIdempotent() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            List<GanttTask> tasks = List.of(new GanttTask("t", MON, 1));
            canvas.render(scale, tasks);
            int count = canvas.getChildren().size();
            canvas.render(scale, tasks);
            assertEquals(count, canvas.getChildren().size());
        });
    }
}
