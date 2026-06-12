package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FxToolkitExtension.class)
class StatusBarTest {

    @Test
    void hasTheFiveLabelledSegments() throws Exception {
        FxThread.run(() -> {
            StatusBar bar = new StatusBar();
            assertNotNull(bar.messageLabel());
            assertNotNull(bar.fileLabel());
            assertNotNull(bar.dirtyLabel());
            assertNotNull(bar.taskCountLabel());
            assertNotNull(bar.zoomLabel());
        });
    }

    @Test
    void startsSane() throws Exception {
        FxThread.run(() -> {
            StatusBar bar = new StatusBar();
            assertEquals("", bar.messageLabel().getText());
            assertEquals(StatusBar.NO_FILE, bar.fileLabel().getText());
            assertFalse(bar.dirtyLabel().isVisible(), "clean: no dirty dot");
            assertEquals("0 tasks", bar.taskCountLabel().getText());
            assertEquals("100%", bar.zoomLabel().getText());
        });
    }

    @Test
    void mountedAsTheBottomRegion() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            assertInstanceOf(StatusBar.class, root.getBottom());
            assertNotNull(root.lookup("#" + RootLayout.STATUS_AREA));
        });
    }
}
