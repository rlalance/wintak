package com.richardl.wintak.controller;

import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.MainMenuBar;
import com.richardl.wintak.view.RootLayout;
import com.richardl.wintak.view.SideToolBar;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class ZoomCommandsTest {

    @Test
    void zoomInAndOutScaleThePxPerDay() {
        MainController controller = new MainController();
        double initial = controller.zoomPxPerDayProperty().get();

        controller.zoomIn();
        assertTrue(controller.zoomPxPerDayProperty().get() > initial);

        controller.zoomOut();
        assertEquals(initial, controller.zoomPxPerDayProperty().get(), 1e-9);
    }

    @Test
    void zoomIsClampedToSaneBounds() {
        MainController controller = new MainController();
        for (int i = 0; i < 100; i++) {
            controller.zoomIn();
        }
        assertTrue(controller.zoomPxPerDayProperty().get() <= MainController.MAX_PX_PER_DAY);

        for (int i = 0; i < 200; i++) {
            controller.zoomOut();
        }
        assertTrue(controller.zoomPxPerDayProperty().get() >= MainController.MIN_PX_PER_DAY);
    }

    @Test
    void zoomToFitGoesThroughTheSeam() {
        MainController controller = new MainController();
        AtomicInteger fits = new AtomicInteger();
        controller.setOnZoomToFit(fits::incrementAndGet);
        controller.zoomToFit();
        assertEquals(1, fits.get());
    }

    @Test
    void toolbarButtonsAndViewMenuShareTheCommands() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            SideToolBar bar = (SideToolBar) root.getLeft();
            double initial = controller.zoomPxPerDayProperty().get();

            Button zoomOut = toolbarButton(bar, "toolbar-zoom-out");
            Button zoomFit = toolbarButton(bar, "toolbar-zoom-fit");
            assertNotNull(zoomOut);
            assertNotNull(zoomFit);

            toolbarButton(bar, "toolbar-zoom-in").fire();
            assertTrue(controller.zoomPxPerDayProperty().get() > initial);

            MainMenuBar menuBar = (MainMenuBar) root.getTop();
            menuBar.item("menu-view-zoom-out").fire();
            assertEquals(initial, controller.zoomPxPerDayProperty().get(), 1e-9);

            AtomicInteger fits = new AtomicInteger();
            controller.setOnZoomToFit(fits::incrementAndGet);
            menuBar.item("menu-view-zoom-fit").fire();
            zoomFit.fire();
            assertEquals(2, fits.get());
        });
    }

    private static Button toolbarButton(SideToolBar bar, String id) {
        return (Button) bar.getItems().stream()
                .filter(n -> id.equals(n.getId())).findFirst().orElseThrow(
                        () -> new AssertionError("no toolbar button " + id));
    }
}
