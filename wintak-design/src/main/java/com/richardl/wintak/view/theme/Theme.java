package com.richardl.wintak.view.theme;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * A theme is pure data: the shared token sheet plus one neutral-palette sheet. Adding a future
 * theme = one enum value + one CSS file in this package; nothing else changes.
 */
public enum Theme {

    DARK("wintak-dark.css"),
    LIGHT("wintak-light.css");

    /** Matches the specimen sheet's {@code data-theme="dark"} default. */
    public static final Theme DEFAULT = DARK;

    private static final String BASE_SHEET = "wintak-base.css";

    private final List<String> stylesheets;

    Theme(String neutralSheet) {
        this.stylesheets = List.of(resolve(BASE_SHEET), resolve(neutralSheet));
    }

    /** Ordered stylesheet URLs: shared tokens first, then this theme's neutrals. */
    public List<String> stylesheets() {
        return stylesheets;
    }

    private static String resolve(String sheet) {
        URL url = Objects.requireNonNull(Theme.class.getResource(sheet),
                "stylesheet not on classpath: " + sheet);
        return url.toExternalForm();
    }
}
