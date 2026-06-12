package com.richardl.wintak.testutil;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class FxToolkitExtensionTest {

    @Test
    void toolkitIsUpAndNodesCanBeConstructed() throws Exception {
        Label label = FxThread.call(() -> new Label("hello"));
        assertEquals("hello", label.getText());
    }

    @Test
    void fxThreadHelperRunsOnTheApplicationThread() throws Exception {
        assertTrue(FxThread.call(Platform::isFxApplicationThread));
    }
}
