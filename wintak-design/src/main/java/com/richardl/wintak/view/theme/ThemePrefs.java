package com.richardl.wintak.view.theme;

import java.util.prefs.Preferences;

/**
 * Persists the chosen theme in a {@link Preferences} node (injected - tests use a scratch
 * node). Unknown or missing stored values fall back to {@link Theme#DEFAULT}.
 */
public final class ThemePrefs {

    static final String KEY = "theme";

    private final Preferences node;

    public ThemePrefs(Preferences node) {
        this.node = node;
    }

    public Theme load() {
        String stored = node.get(KEY, Theme.DEFAULT.name());
        try {
            return Theme.valueOf(stored);
        } catch (IllegalArgumentException unknown) {
            return Theme.DEFAULT;
        }
    }

    public void save(Theme theme) {
        node.put(KEY, theme.name());
    }

    /** Restores the stored theme now and persists every future switch. */
    public void bind(ThemeManager manager) {
        manager.themeProperty().set(load());
        manager.themeProperty().addListener((obs, old, next) -> save(next));
    }
}
