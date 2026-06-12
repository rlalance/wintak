package com.richardl.wintak.app;

import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.Scene;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FxToolkitExtension.class)
class WintakAppTest {

    @Test
    void buildSceneProducesASceneWithARoot() throws Exception {
        Scene scene = FxThread.call(WintakApp::buildScene);
        assertNotNull(scene.getRoot(), "scene must have a root node");
    }

    @Test
    void applicationTitleIsWintak() {
        assertEquals("Wintak", WintakApp.APP_TITLE);
    }
}
