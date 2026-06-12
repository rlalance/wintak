package com.richardl.wintak.view;

import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.TimeScale;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.shape.Polyline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class DependencyArrowTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8);

    @Test
    void anElbowConnectsPredecessorFinishToSuccessorStart() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask design = new GanttTask("Design", MON, 5);          // row 0, ends x=100
            GanttTask build = new GanttTask("Build", MON.plusDays(7), 10); // row 1, starts x=140

            canvas.render(scale, List.of(design, build),
                    List.of(new Dependency(design, build)));

            List<Polyline> arrows = canvas.arrows();
            assertEquals(1, arrows.size());

            List<Double> pts = arrows.get(0).getPoints();
            double fromX = pts.get(0);
            double fromY = pts.get(1);
            double toX = pts.get(pts.size() - 2);
            double toY = pts.get(pts.size() - 1);

            assertEquals(100, fromX, 1e-9, "leaves the predecessor's finish edge");
            assertEquals(BarCanvas.ROW_HEIGHT / 2, fromY, 1e-9, "vertically centred on row 0");
            assertEquals(140, toX, 1e-9, "arrives at the successor's start edge");
            assertEquals(BarCanvas.ROW_HEIGHT * 1.5, toY, 1e-9, "vertically centred on row 1");
        });
    }

    @Test
    void elbowFromMilestoneStartsAtDiamondRightTip() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask ms = new GanttTask("Gate", MON, 1);
            ms.setMilestone(true);
            GanttTask build = new GanttTask("Build", MON.plusDays(7), 10);

            canvas.render(scale, List.of(ms, build),
                    List.of(new Dependency(ms, build)));

            double fromX = canvas.arrows().get(0).getPoints().get(0);
            double expectedFromX = scale.xOf(MON) + BarCanvas.BAR_HEIGHT / 2.0;
            assertEquals(expectedFromX, fromX, 1e-9, "arrow must leave from the diamond's right tip, not durationDays*px");
        });
    }

    @Test
    void elbowToMilestoneArrivesAtDiamondLeftTip() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            TimeScale scale = new TimeScale(MON, 20);
            GanttTask build = new GanttTask("Build", MON, 10);
            GanttTask ms = new GanttTask("Gate", MON.plusDays(10), 1);
            ms.setMilestone(true);

            canvas.render(scale, List.of(build, ms),
                    List.of(new Dependency(build, ms)));

            javafx.collections.ObservableList<Double> pts = canvas.arrows().get(0).getPoints();
            double toX = pts.get(pts.size() - 2);
            double expectedToX = scale.xOf(MON.plusDays(10)) - BarCanvas.BAR_HEIGHT / 2.0;
            assertEquals(expectedToX, toX, 1e-9, "arrow must arrive at the diamond's left tip");
        });
    }

    @Test
    void noDependenciesMeansNoArrows() throws Exception {
        FxThread.run(() -> {
            BarCanvas canvas = new BarCanvas();
            canvas.render(new TimeScale(MON, 20), List.of(new GanttTask("t", MON, 1)));
            assertTrue(canvas.arrows().isEmpty());
        });
    }
}
