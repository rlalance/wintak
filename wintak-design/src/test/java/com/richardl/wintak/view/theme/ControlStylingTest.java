package com.richardl.wintak.view.theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 2026-06-11: the Wintak stylesheet covers every UI component, not just the chrome -
 * each control family has token-based rules per the specimen sheet in Design/.
 */
class ControlStylingTest {

    @ParameterizedTest
    @ValueSource(strings = {
            ".button", ".button:default", ".toggle-button:selected",
            ".radio-button", ".check-box", ".check-box:selected",
            ".text-input", ".text-input:focused",
            ".combo-box", ".choice-box", ".menu-button", ".spinner",
            ".slider", ".slider .thumb",
            ".progress-bar", ".progress-bar > .bar", ".progress-indicator",
            ".date-picker", ".color-picker", ".hyperlink",
            ".list-view", ".list-cell:filled:selected", ".list-cell:odd",
            ".table-view", ".column-header", ".table-row-cell:filled:selected",
            ".tree-view", ".tree-cell",
            ".tab-pane", ".tab:selected", ".titled-pane > .title",
            ".pagination", ".scroll-bar", ".scroll-bar .thumb",
            ".separator", ".tooltip",
    })
    void everyControlFamilyIsStyled(String selector) {
        assertTrue(baseCss().contains(selector),
                "wintak-base.css must style " + selector + " (specimen sheet coverage)");
    }

    @Test
    void modenaDerivationHooksAreMappedToTokens() {
        String css = baseCss();
        assertTrue(css.contains("-fx-accent: -wintak-accent"),
                "selection/accent must derive from the token");
        assertTrue(css.contains("-fx-focus-color: -wintak-accent-focus"),
                "focus rings must derive from the token");
    }

    @Test
    void checkMarksUseTheReservedCheckColour() {
        // the specimen reserves the cyan 'check' token for affirmation marks
        String css = baseCss();
        int mark = css.indexOf(".check-box:selected");
        assertTrue(mark >= 0 && css.indexOf("-wintak-check", mark) > mark);
    }

    @Test
    void noRawHexColoursOutsideTheTokenBlocks() {
        // every rule below the token definitions must reference tokens, not literals
        String css = baseCss();
        String afterTokens = css.substring(css.indexOf("CHROME"));
        assertFalse(afterTokens.matches("(?s).*#[0-9a-fA-F]{3,8}\\b.*"),
                "component rules must use looked-up colours, never raw hex");
    }

    private static String baseCss() {
        try (InputStream in = ControlStylingTest.class.getResourceAsStream("wintak-base.css")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
