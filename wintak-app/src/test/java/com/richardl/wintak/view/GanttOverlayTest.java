package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class GanttOverlayTest {

    @Test
    void ganttOverlayHiddenByDefault() {
        assertFalse(new MainController().ganttOverlayVisibleProperty().get());
    }

    @Test
    void ganttEditorIsFullyOpaqueWhenVisible() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            controller.ganttOverlayVisibleProperty().set(true);
            assertEquals(1.0, root.lookup("#gantt-editor").getOpacity(), 0.001);
        });
    }

    @Test
    void ganttEditorIsInteractiveWhenVisible() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            controller.ganttOverlayVisibleProperty().set(true);
            assertFalse(root.lookup("#gantt-editor").isMouseTransparent(),
                    "Gantt must accept mouse input when visible");
        });
    }

    @Test
    void ganttEditorHiddenByDefault() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            assertFalse(root.lookup("#gantt-editor").isVisible());
        });
    }

    @Test
    void ganttEditorHidesWhenOverlayDisabled() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            controller.ganttOverlayVisibleProperty().set(true);
            controller.ganttOverlayVisibleProperty().set(false);
            assertFalse(root.lookup("#gantt-editor").isVisible());
        });
    }

    @Test
    void viewMenuHasGanttCheckItem() throws Exception {
        FxThread.run(() -> {
            MainMenuBar bar = new MainMenuBar();
            MenuItem item = bar.item("menu-view-gantt");
            assertNotNull(item);
            assertInstanceOf(CheckMenuItem.class, item);
        });
    }

    @Test
    void ganttMenuItemSyncsWithController() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            CheckMenuItem item = (CheckMenuItem) ((MainMenuBar) root.getTop()).item("menu-view-gantt");

            assertFalse(item.isSelected(), "starts unselected (overlay hidden)");

            item.setSelected(true);
            assertTrue(controller.ganttOverlayVisibleProperty().get(), "menu->controller");

            controller.ganttOverlayVisibleProperty().set(false);
            assertFalse(item.isSelected(), "controller->menu");
        });
    }
}
