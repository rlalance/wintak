package com.richardl.wintak.view.theme;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertSame;

class ThemePrefsTest {

    private final Preferences node =
            Preferences.userRoot().node("wintak-test/" + UUID.randomUUID());

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void missingValueFallsBackToDark() {
        assertSame(Theme.DARK, new ThemePrefs(node).load());
    }

    @Test
    void garbageValueFallsBackToDark() {
        node.put(ThemePrefs.KEY, "SOLARIZED_UNICORN");
        assertSame(Theme.DARK, new ThemePrefs(node).load());
    }

    @Test
    void saveThenLoadRoundTrips() {
        ThemePrefs prefs = new ThemePrefs(node);
        prefs.save(Theme.LIGHT);
        assertSame(Theme.LIGHT, prefs.load());
    }

    @Test
    void bindRestoresTheStoredThemeAndPersistsChanges() {
        node.put(ThemePrefs.KEY, Theme.LIGHT.name());
        ThemePrefs prefs = new ThemePrefs(node);
        ThemeManager manager = new ThemeManager();

        prefs.bind(manager);
        assertSame(Theme.LIGHT, manager.themeProperty().get(), "stored theme restored at startup");

        manager.themeProperty().set(Theme.DARK);
        assertSame(Theme.DARK, prefs.load(), "subsequent switches are persisted");
    }
}
