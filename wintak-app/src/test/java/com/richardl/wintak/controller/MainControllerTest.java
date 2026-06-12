package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.MainMenuBar;
import com.richardl.wintak.view.RootLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class MainControllerTest {

    @Test
    void startsWithACleanUntitledDocument() {
        MainController controller = new MainController();
        assertFalse(controller.getDocument().isDirty());
        assertSame(GanttDocument.DEFAULT_TITLE, controller.getDocument().getTitle());
    }

    @Test
    void fileNewReplacesTheDocumentWithACleanOne() {
        MainController controller = new MainController();
        GanttDocument before = controller.getDocument();
        before.addTask(new GanttTask("t", LocalDate.of(2026, 6, 8), 1));

        controller.newDocument();

        assertNotSame(before, controller.getDocument());
        assertTrue(controller.getDocument().getTasks().isEmpty());
        assertFalse(controller.getDocument().isDirty());
    }

    @Test
    void documentIsObservableSoViewsCanRebind() {
        MainController controller = new MainController();
        AtomicBoolean swapped = new AtomicBoolean();
        controller.documentProperty().addListener((obs, old, next) -> swapped.set(true));
        controller.newDocument();
        assertTrue(swapped.get());
    }

    @Test
    void exitDelegatesToTheCloseSeam() {
        MainController controller = new MainController();
        AtomicBoolean closed = new AtomicBoolean();
        controller.setOnCloseRequest(() -> closed.set(true));
        controller.exit();
        assertTrue(closed.get());
    }

    @Test
    void menuItemsFireControllerCommands() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            GanttDocument before = controller.getDocument();

            MainMenuBar menuBar = (MainMenuBar) root.getTop();
            menuBar.item("menu-file-new").fire();
            assertNotSame(before, controller.getDocument());

            AtomicBoolean closed = new AtomicBoolean();
            controller.setOnCloseRequest(() -> closed.set(true));
            menuBar.item("menu-file-exit").fire();
            assertTrue(closed.get());
        });
    }
}
