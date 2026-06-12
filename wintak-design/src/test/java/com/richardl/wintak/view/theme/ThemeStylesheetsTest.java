package com.richardl.wintak.view.theme;

import javafx.css.CssParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The design-token stylesheets mirror Design/WinTAK Specimen Sheet (standalone).html -
 * shared tokens in wintak-base.css, neutral palettes in wintak-dark.css / wintak-light.css.
 */
class ThemeStylesheetsTest {

    private static final String CSS_ROOT = "/com/richardl/wintak/view/theme/";

    private static final String[] SHARED_TOKENS = {
            "-wintak-accent", "-wintak-accent-2", "-wintak-accent-hover", "-wintak-accent-focus",
            "-wintak-check", "-wintak-success", "-wintak-success-bg", "-wintak-warning",
            "-wintak-warning-bg", "-wintak-error", "-wintak-error-bg",
            "-wintak-maroon", "-wintak-maroon-accent",
    };

    private static final String[] NEUTRAL_TOKENS = {
            "-wintak-bg", "-wintak-surface-1", "-wintak-surface-2", "-wintak-surface-3",
            "-wintak-surface-raised", "-wintak-field-bg",
            "-wintak-border", "-wintak-border-strong", "-wintak-border-neutral",
            "-wintak-text", "-wintak-text-dim", "-wintak-text-mute", "-wintak-text-faint",
            "-wintak-disabled-fill", "-wintak-disabled-border", "-wintak-disabled-text",
            "-wintak-row-alt", "-wintak-hover-tint", "-wintak-code-bg",
    };

    @Test
    void baseSheetDefinesEverySharedToken() {
        String css = read("wintak-base.css");
        for (String token : SHARED_TOKENS) {
            assertTrue(css.contains(token + ":"), "wintak-base.css missing " + token);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"wintak-dark.css", "wintak-light.css"})
    void themeSheetsDefineEveryNeutralToken(String sheet) {
        String css = read(sheet);
        for (String token : NEUTRAL_TOKENS) {
            assertTrue(css.contains(token + ":"), sheet + " missing " + token);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"wintak-base.css", "wintak-dark.css", "wintak-light.css"})
    void stylesheetsParseAsValidJavaFxCss(String sheet) throws IOException {
        CssParser.errorsProperty().clear();
        new CssParser().parse(url(sheet));
        assertTrue(CssParser.errorsProperty().isEmpty(),
                sheet + " has CSS errors: " + CssParser.errorsProperty());
    }

    private static URL url(String sheet) {
        URL url = ThemeStylesheetsTest.class.getResource(CSS_ROOT + sheet);
        assertNotNull(url, "classpath resource not found: " + CSS_ROOT + sheet);
        return url;
    }

    private static String read(String sheet) {
        try (var in = url(sheet).openStream()) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
