package com.richardl.wintak.view;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(FxToolkitExtension.class)
class MapLayerTest {

    @Test
    void editorAreaContainsMapLayer() throws Exception {
        RootLayout root = FxThread.call(() -> new RootLayout(new MainController()));
        StackPane editorArea = (StackPane) root.lookup("#editor-area");
        assertNotNull(editorArea.lookup("#map-layer"), "map-layer node must be present in editor area");
    }

    @Test
    void mapLayerIsBeneathGanttEditor() throws Exception {
        RootLayout root = FxThread.call(() -> new RootLayout(new MainController()));
        StackPane editorArea = (StackPane) root.lookup("#editor-area");
        assertEquals(2, editorArea.getChildren().size(), "editor area must stack map + gantt");
        assertEquals("map-layer", editorArea.getChildren().get(0).getId(), "map must be bottom layer");
        assertEquals("gantt-editor", editorArea.getChildren().get(1).getId(), "gantt must be top layer");
    }
}
