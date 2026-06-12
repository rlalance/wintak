package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.model.Dependency;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class GanttEditorTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void mountedInTheEditorAreaWithTableHeaderAndCanvas() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            assertEquals(RootLayout.EDITOR_AREA, root.getCenter().getId());
            GanttEditor editor = (GanttEditor) root.lookup("#gantt-editor");
            assertNotNull(editor, "the editor lives inside the editor-area stack");
            assertNotNull(editor.table());
            assertNotNull(editor.header());
            assertNotNull(editor.canvas());
        });
    }

    @Test
    void barsFollowTheDocumentLive() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);

            assertEquals(0, editor.canvas().bars().size());
            GanttTask task = new GanttTask("t", DAY, 5);
            controller.getDocument().addTask(task);
            assertEquals(1, editor.canvas().bars().size());

            controller.getDocument().removeTask(task);
            assertEquals(0, editor.canvas().bars().size());
        });
    }

    @Test
    void editingATaskRerendersItsBar() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask task = new GanttTask("t", DAY, 5);
            controller.getDocument().addTask(task);
            double width = editor.canvas().bars().get(0).getPrefWidth();

            task.setDurationDays(10);
            assertEquals(width * 2, editor.canvas().bars().get(0).getPrefWidth(), 1e-9);
        });
    }

    @Test
    void zoomChangesRescaleTheBars() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            controller.getDocument().addTask(new GanttTask("t", DAY, 5));
            double width = editor.canvas().bars().get(0).getPrefWidth();

            controller.zoomIn();
            assertEquals(width * 1.25, editor.canvas().bars().get(0).getPrefWidth(), 1e-9);
        });
    }

    @Test
    void documentSwapRerendersEverything() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            controller.getDocument().addTask(new GanttTask("old", DAY, 1));

            controller.newDocument();
            assertEquals(0, editor.canvas().bars().size());

            controller.getDocument().addTask(new GanttTask("fresh", DAY, 2));
            assertEquals(1, editor.canvas().bars().size());
        });
    }

    @Test
    void fitMathFillsTheViewportAndClamps() {
        assertEquals(10, GanttEditor.fitPxPerDay(100, 10), 1e-9);
        assertEquals(MainController.MAX_PX_PER_DAY, GanttEditor.fitPxPerDay(10_000, 10), 1e-9);
        assertEquals(MainController.MIN_PX_PER_DAY, GanttEditor.fitPxPerDay(100, 10_000), 1e-9);
    }

    @Test
    void selectedMilestoneGetsSelectedStyleOnDiamond() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask ms = new GanttTask("Gate", DAY, 1);
            ms.setMilestone(true);
            controller.getDocument().addTask(ms);

            controller.selectedTaskProperty().set(ms);

            Polygon diamond = editor.canvas().diamonds().get(0);
            assertTrue(diamond.getStyleClass().contains("selected"),
                    "a selected milestone diamond must carry the .selected class");
        });
    }

    @Test
    void selectedDependencyArrowGetsSelectedStyle() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask a = new GanttTask("a", DAY, 5);
            GanttTask b = new GanttTask("b", DAY.plusDays(10), 5);
            controller.getDocument().addTask(a);
            controller.getDocument().addTask(b);
            controller.getDocument().addDependency(a, b);

            Dependency link = controller.getDocument().getDependencies().get(0);
            controller.selectedDependencyProperty().set(link);

            Polyline arrow = editor.canvas().arrows().get(0);
            assertTrue(arrow.getStyleClass().contains("selected"),
                    "the selected dependency's arrow must carry the .selected class");
        });
    }

    @Test
    void emptyCanvasClickClearsBothSelections() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            GanttTask a = new GanttTask("a", DAY, 5);
            GanttTask b = new GanttTask("b", DAY.plusDays(10), 5);
            controller.getDocument().addTask(a);
            controller.getDocument().addTask(b);
            controller.getDocument().addDependency(a, b);

            Dependency link = controller.getDocument().getDependencies().get(0);
            controller.selectedDependencyProperty().set(link);

            // Fire a canvas click far from any bar or arrow (-999, -999)
            editor.canvas().fireEvent(click());

            assertNull(controller.selectedDependencyProperty().get(),
                    "clicking empty canvas must clear selectedDependency");
            assertNull(controller.selectedTaskProperty().get(),
                    "clicking empty canvas must also clear selectedTask");
        });
    }

    @Test
    void zoomToFitCommandReachesTheEditor() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            GanttEditor editor = new GanttEditor(controller);
            controller.getDocument().addTask(new GanttTask("t", DAY, 400));

            controller.zoomToFit();
            assertTrue(controller.zoomPxPerDayProperty().get() < MainController.DEFAULT_PX_PER_DAY,
                    "400 days cannot fit at the default zoom, so fit must zoom out");
        });
    }

    private static MouseEvent click() {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, -999, -999, -999, -999,
                MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, true, null);
    }
}
