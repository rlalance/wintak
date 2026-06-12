package com.richardl.wintak.view.theme;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

/**
 * Applies the active {@link Theme} to a Scene and re-applies it whenever the theme property
 * changes. The property is the single source of truth the View menu binds to (FEAT-36).
 */
public final class ThemeManager {

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(this, "theme", Theme.DEFAULT);

    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    /** Themes the scene now and on every future theme change. */
    public void attach(Scene scene) {
        apply(scene, theme.get());
        theme.addListener((obs, old, next) -> apply(scene, next));
    }

    private static void apply(Scene scene, Theme theme) {
        scene.getStylesheets().setAll(theme.stylesheets());
    }
}
