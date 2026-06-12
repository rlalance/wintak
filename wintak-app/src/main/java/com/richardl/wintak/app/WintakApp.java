package com.richardl.wintak.app;

import com.richardl.wintak.controller.AutoSaveCoalescer;
import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.view.ComponentSheet;
import com.richardl.wintak.view.RootLayout;
import com.richardl.wintak.view.theme.ThemePrefs;
import javafx.application.Application;

import java.time.Duration;
import java.util.prefs.Preferences;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Application entry point. All scene assembly lives in {@link #buildScene} so tests can use it. */
public class WintakApp extends Application {

    public static final String APP_TITLE = "Wintak";

    /**
     * The app's language is English regardless of the OS locale - built-in controls
     * (DatePicker, ColorPicker, ...) localise themselves from the JVM default otherwise.
     */
    public static void pinEnglishLocale() {
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
    }

    public static Scene buildScene() {
        return buildScene(new MainController());
    }

    /** Chrome plus the fullscreen component-sheet overlay (FEAT-45) stacked above it. */
    public static Scene buildScene(MainController controller) {
        ComponentSheet sheet = new ComponentSheet(controller.getThemeManager());
        sheet.setVisible(false);
        sheet.visibleProperty().bindBidirectional(controller.componentSheetVisibleProperty());
        sheet.managedProperty().bind(sheet.visibleProperty());
        sheet.visibleProperty().addListener((obs, was, shown) -> {
            if (shown) {
                sheet.requestFocus(); // so Esc lands on the overlay
            }
        });

        Scene scene = new Scene(new StackPane(new RootLayout(controller), sheet), 1280, 800);
        controller.getThemeManager().attach(scene);
        return scene;
    }

    @Override
    public void start(Stage stage) {
        pinEnglishLocale();
        MainController controller = new MainController();
        controller.setOnCloseRequest(stage::close);
        controller.setFileDialogs(new FxFileDialogs(stage));
        controller.setUserPrompts(new FxUserPrompts(stage));
        controller.setDelayedRunner(new FxDelayedRunner());
        controller.setAutoSaveCoalescer(new AutoSaveCoalescer(
                Duration.ofSeconds(2), () -> controller.autoSave(), new FxDelayedRunner()));
        new ThemePrefs(Preferences.userNodeForPackage(WintakApp.class))
                .bind(controller.getThemeManager());
        stage.setScene(buildScene(controller));
        stage.titleProperty().bind(controller.windowTitleProperty());
        // The window X must honour the same unsaved-changes gate as File -> Exit.
        stage.setOnCloseRequest(event -> {
            event.consume();
            controller.exit();
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
