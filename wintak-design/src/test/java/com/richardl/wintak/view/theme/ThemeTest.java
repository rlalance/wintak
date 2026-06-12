package com.richardl.wintak.view.theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeTest {

    @Test
    void darkIsTheDefaultThemeMatchingTheSpecimenSheet() {
        assertSame(Theme.DARK, Theme.DEFAULT);
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void everyThemeLoadsBaseTokensFirstThenItsOwnSheet(Theme theme) {
        assertEquals(2, theme.stylesheets().size(), "base sheet + one neutral sheet");
        assertTrue(theme.stylesheets().get(0).endsWith("wintak-base.css"),
                "shared tokens must load first so the theme sheet can rely on them");
    }

    @ParameterizedTest
    @EnumSource(Theme.class)
    void stylesheetUrlsAreResolvableExternalForms(Theme theme) {
        for (String url : theme.stylesheets()) {
            assertTrue(url.startsWith("file:") || url.startsWith("jar:"),
                    "expected a resolved external-form URL, got: " + url);
        }
    }
}
