package com.richardl.wintak.view.theme;

import com.richardl.wintak.controller.MainController;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import com.richardl.wintak.view.RootLayout;
import com.richardl.wintak.view.StatusBar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** FEAT-35: the chrome is styled exclusively through the design-token sheets. */
@ExtendWith(FxToolkitExtension.class)
class ChromeStylingTest {

    @ParameterizedTest
    @ValueSource(strings = {
            ".menu-bar", ".tool-bar", ".status-bar", "#editor-area",
            ".menu-item:focused", ".tool-bar .toggle-button:selected", "#status-dirty",
    })
    void baseSheetStylesEveryChromeRegion(String selector) {
        assertTrue(baseCss().contains(selector), "wintak-base.css must style " + selector);
    }

    @Test
    void brandBarUsesTheReservedMaroon() {
        String css = baseCss();
        int menuBar = css.indexOf(".menu-bar");
        assertTrue(menuBar >= 0 && css.indexOf("-wintak-maroon", menuBar) > menuBar,
                "the top bar carries the brand maroon");
    }

    @Test
    void statusBarCarriesItsStyleClass() throws Exception {
        FxThread.run(() -> assertTrue(new StatusBar().getStyleClass().contains("status-bar")));
    }

    @Test
    void noInlineStylesAnywhereInTheChrome() throws Exception {
        FxThread.run(() -> {
            RootLayout root = new RootLayout(new MainController());
            root.lookupAll("*").forEach(node ->
                    assertTrue(node.getStyle() == null || node.getStyle().isEmpty(),
                            "inline style on " + node + " \u2014 use the token sheets"));
        });
    }

    private static String baseCss() {
        try (InputStream in = ChromeStylingTest.class.getResourceAsStream("wintak-base.css")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
