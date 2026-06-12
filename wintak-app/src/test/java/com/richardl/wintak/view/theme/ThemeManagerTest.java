package com.richardl.wintak.view.theme;

import com.richardl.wintak.app.WintakApp;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class ThemeManagerTest {

    @Test
    void attachingAppliesTheDefaultTheme() throws Exception {
        FxThread.run(() -> {
            ThemeManager manager = new ThemeManager();
            Scene scene = new Scene(new StackPane());
            manager.attach(scene);
            assertSame(Theme.DEFAULT, manager.themeProperty().get());
            assertEquals(Theme.DEFAULT.stylesheets(), scene.getStylesheets());
        });
    }

    @Test
    void changingTheThemePropertySwapsTheSceneStylesheets() throws Exception {
        FxThread.run(() -> {
            ThemeManager manager = new ThemeManager();
            Scene scene = new Scene(new StackPane());
            manager.attach(scene);
            manager.themeProperty().set(Theme.LIGHT);
            assertEquals(Theme.LIGHT.stylesheets(), scene.getStylesheets());
            manager.themeProperty().set(Theme.DARK);
            assertEquals(Theme.DARK.stylesheets(), scene.getStylesheets());
        });
    }

    @Test
    void appSceneLaunchesThemed() throws Exception {
        Scene scene = FxThread.call(WintakApp::buildScene);
        assertEquals(Theme.DEFAULT.stylesheets(), scene.getStylesheets());
        assertTrue(scene.getStylesheets().get(0).endsWith("wintak-base.css"));
    }
}
