package com.richardl.wintak.view;

import com.richardl.wintak.app.WintakApp;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class ComponentSheetIntegrationTest {

    @Test
    void overlayIsFullscreenAboveTheChrome() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            Scene scene = WintakApp.buildScene(controller);

            Node sheet = scene.getRoot().lookup("#component-sheet");
            assertNotNull(sheet, "buildScene must mount the component sheet");
            assertSame(scene.getRoot(), sheet.getParent(),
                    "the sheet stacks at scene level, above the entire chrome");
            assertTrue(scene.getRoot() instanceof StackPane);
            assertEquals(2, ((StackPane) scene.getRoot()).getChildren().size());
            assertTrue(((StackPane) scene.getRoot()).getChildren().get(0) instanceof RootLayout,
                    "the chrome sits underneath the overlay");
            assertFalse(sheet.isVisible(), "hidden by default");
        });
    }

    @Test
    void menuToggleShowsAndHidesTheOverlay() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            Scene scene = WintakApp.buildScene(controller);
            RootLayout root = (RootLayout) ((StackPane) scene.getRoot()).getChildren().get(0);
            MainMenuBar menuBar = (MainMenuBar) root.getTop();
            Node sheet = scene.getRoot().lookup("#component-sheet");

            CheckMenuItem toggle = (CheckMenuItem) menuBar.item("menu-view-component-sheet");
            toggle.setSelected(true);
            assertTrue(sheet.isVisible());

            toggle.setSelected(false);
            assertFalse(sheet.isVisible());
        });
    }

    @Test
    void escapeClosesTheOverlay() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            Scene scene = WintakApp.buildScene(controller);
            Node sheet = scene.getRoot().lookup("#component-sheet");
            controller.componentSheetVisibleProperty().set(true);

            sheet.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));

            assertFalse(controller.componentSheetVisibleProperty().get());
            assertFalse(sheet.isVisible());
        });
    }

    @Test
    void editorStillReachableUnderTheOverlay() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            assertNotNull(root.lookup("#" + RootLayout.EDITOR_AREA));
            assertTrue(anyMatch(root, n -> n instanceof GanttEditor));
        });
    }

    private static boolean anyMatch(Node node, java.util.function.Predicate<Node> test) {
        if (test.test(node)) {
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (anyMatch(child, test)) {
                    return true;
                }
            }
            if (node instanceof javafx.scene.control.ScrollPane scroll && scroll.getContent() != null) {
                return anyMatch(scroll.getContent(), test);
            }
        }
        return false;
    }
}
