package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.controller.ToolMode;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class SideToolBarTest {

    @Test
    void verticalBarWithOneTogglePerToolInOrder() throws Exception {
        FxThread.run(() -> {
            SideToolBar bar = new SideToolBar(new MainController());
            assertEquals(Orientation.VERTICAL, bar.getOrientation());
            assertEquals(
                    List.of("tool-select", "tool-add-task", "tool-add-milestone", "tool-link", "tool-pan"),
                    bar.toolToggles().stream().map(ToggleButton::getId).toList());
            bar.toolToggles().forEach(t -> assertNotNull(t.getTooltip(), t.getId() + " needs a tooltip"));
        });
    }

    @Test
    void defaultSelectionIsTheSelectTool() throws Exception {
        FxThread.run(() -> {
            SideToolBar bar = new SideToolBar(new MainController());
            assertTrue(bar.toggleFor(ToolMode.SELECT).isSelected());
        });
    }

    @Test
    void selectingAToggleDrivesTheControllerAndViceVersa() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            SideToolBar bar = new SideToolBar(controller);

            bar.toggleFor(ToolMode.LINK).setSelected(true);
            assertSame(ToolMode.LINK, controller.toolModeProperty().get());

            controller.toolModeProperty().set(ToolMode.PAN);
            assertTrue(bar.toggleFor(ToolMode.PAN).isSelected());
        });
    }

    @Test
    void exactlyOneToolStaysActive() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            SideToolBar bar = new SideToolBar(controller);

            // clicking the already-active toggle must not leave the app toolless
            bar.toggleFor(ToolMode.SELECT).setSelected(false);
            assertTrue(bar.toggleFor(ToolMode.SELECT).isSelected());
            assertSame(ToolMode.SELECT, controller.toolModeProperty().get());
        });
    }

    @Test
    void toolToggleButtonsUseFontIconGraphics() throws Exception {
        FxThread.run(() -> {
            SideToolBar bar = new SideToolBar(new MainController());
            bar.toolToggles().forEach(t -> {
                assertInstanceOf(FontIcon.class, t.getGraphic(),
                        t.getId() + " must carry a FontIcon graphic");
                assertTrue(t.getText() == null || t.getText().isEmpty(),
                        t.getId() + " must not use raw text glyphs");
            });
        });
    }

    @Test
    void zoomButtonsUseFontIconGraphics() throws Exception {
        FxThread.run(() -> {
            SideToolBar bar = new SideToolBar(new MainController());
            bar.getItems().stream()
                    .filter(n -> n instanceof Button)
                    .map(n -> (Button) n)
                    .forEach(b -> {
                        assertInstanceOf(FontIcon.class, b.getGraphic(),
                                b.getId() + " must carry a FontIcon graphic");
                        assertTrue(b.getText() == null || b.getText().isEmpty(),
                                b.getId() + " must not use raw text glyphs");
                    });
        });
    }

    @Test
    void mountedAsTheLeftRegion() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            assertInstanceOf(SideToolBar.class, root.getLeft());
            assertNotNull(root.lookup("#" + RootLayout.TOOLBAR_AREA));
        });
    }

    @Test
    void visibilityTracksGanttOverlayProperty() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            assertFalse(root.getLeft().isVisible(), "hidden when gantt overlay is off (default)");
            controller.ganttOverlayVisibleProperty().set(true);
            assertTrue(root.getLeft().isVisible(), "visible when gantt overlay is on");
            controller.ganttOverlayVisibleProperty().set(false);
            assertFalse(root.getLeft().isVisible(), "hidden again when gantt overlay is off");
        });
    }
}
