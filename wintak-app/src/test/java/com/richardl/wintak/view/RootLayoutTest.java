package com.richardl.wintak.view;

import com.richardl.wintak.app.WintakApp;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.Scene;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(FxToolkitExtension.class)
class RootLayoutTest {

    @ParameterizedTest
    @ValueSource(strings = {"menu-area", "toolbar-area", "editor-area", "status-area"})
    void shellExposesEachChromeRegionById(String id) throws Exception {
        RootLayout root = FxThread.call(() -> new RootLayout(new MainController()));
        assertNotNull(root.lookup("#" + id), "missing region #" + id);
    }

    @Test
    void regionsSitInTheExpectedBorderPaneSlots() throws Exception {
        RootLayout root = FxThread.call(() -> new RootLayout(new MainController()));
        assertSame(root.getTop(), root.lookup("#menu-area"));
        assertSame(root.getLeft(), root.lookup("#toolbar-area"));
        assertSame(root.getCenter(), root.lookup("#editor-area"));
        assertSame(root.getBottom(), root.lookup("#status-area"));
    }

    @Test
    void appSceneUsesTheShellAsRoot() throws Exception {
        Scene scene = FxThread.call(WintakApp::buildScene);
        assertNotNull(scene.getRoot().lookup("#editor-area"), "buildScene() must mount RootLayout");
    }
}
