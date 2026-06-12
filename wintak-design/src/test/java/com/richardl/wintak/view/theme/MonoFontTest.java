package com.richardl.wintak.view.theme;

import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.text.Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Task 2026-06-11: mono-classed text rendered garbage glyphs. JavaFX -fx-font-family takes
 * ONE family name - a CSS-style fallback list ("Cascadia Code", "Consolas", monospace) is
 * fuzzy-matched as a single unknown name and can resolve to a symbol font.
 */
@ExtendWith(FxToolkitExtension.class)
class MonoFontTest {

    private static final Pattern FONT_FAMILY =
            Pattern.compile("-fx-font-family:\\s*([^;]+);");

    @Test
    void everyFontFamilyDeclarationNamesExactlyOneFamily() {
        for (String sheet : new String[]{"wintak-base.css", "wintak-dark.css", "wintak-light.css"}) {
            Matcher m = FONT_FAMILY.matcher(css(sheet));
            while (m.find()) {
                String value = m.group(1).trim();
                assertFalse(value.contains(","),
                        sheet + ": -fx-font-family must name ONE family, found: " + value);
            }
        }
    }

    @Test
    void everyDeclaredFontFamilyIsInstalled() {
        Matcher m = FONT_FAMILY.matcher(css("wintak-base.css"));
        boolean any = false;
        while (m.find()) {
            any = true;
            String family = m.group(1).trim().replace("\"", "");
            assertTrue(Font.getFamilies().contains(family),
                    "font family '" + family + "' is not installed - it would fuzzy-match garbage");
        }
        assertTrue(any, "expected at least one -fx-font-family declaration");
    }

    private static String css(String sheet) {
        try (InputStream in = MonoFontTest.class.getResourceAsStream(sheet)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
