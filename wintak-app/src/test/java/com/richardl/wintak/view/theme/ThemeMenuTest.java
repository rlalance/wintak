package com.richardl.wintak.view.theme;

import com.richardl.wintak.app.WintakApp;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.MainMenuBar;
import com.richardl.wintak.view.RootLayout;
import javafx.scene.Scene;
import javafx.scene.control.RadioMenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class ThemeMenuTest {

    @Test
    void darkIsSelectedInitially() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            MainMenuBar menuBar = (MainMenuBar) root.getTop();
            assertTrue(((RadioMenuItem) menuBar.item("menu-view-theme-dark")).isSelected());
        });
    }

    @Test
    void choosingARadioSwitchesTheTheme() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            MainMenuBar menuBar = (MainMenuBar) root.getTop();

            ((RadioMenuItem) menuBar.item("menu-view-theme-light")).setSelected(true);
            assertSame(Theme.LIGHT, controller.getThemeManager().themeProperty().get());
        });
    }

    @Test
    void settingTheThemeSelectsTheMatchingRadio() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            RootLayout root = new RootLayout(controller);
            MainMenuBar menuBar = (MainMenuBar) root.getTop();

            controller.getThemeManager().themeProperty().set(Theme.LIGHT);
            assertTrue(((RadioMenuItem) menuBar.item("menu-view-theme-light")).isSelected());
        });
    }

    @Test
    void switchingThemesLiveRestylesTheAppScene() throws Exception {
        FxThread.run(() -> {
            MainController controller = new MainController();
            Scene scene = WintakApp.buildScene(controller);
            assertEquals(Theme.DARK.stylesheets(), scene.getStylesheets());

            controller.getThemeManager().themeProperty().set(Theme.LIGHT);
            assertEquals(Theme.LIGHT.stylesheets(), scene.getStylesheets());
        });
    }
}
