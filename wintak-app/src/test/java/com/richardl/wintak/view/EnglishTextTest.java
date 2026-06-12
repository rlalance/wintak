package com.richardl.wintak.view;

import com.richardl.wintak.model.TimeScale;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** All user-visible text is English regardless of the OS locale (Task 2026-06-11). */
@ExtendWith(FxToolkitExtension.class)
class EnglishTextTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8); // a Monday in June

    @Test
    void timelineRulerUsesEnglishMonthsEvenOnAFrenchSystem() throws Exception {
        Locale system = Locale.getDefault();
        try {
            Locale.setDefault(Locale.CANADA_FRENCH);
            FxThread.run(() -> {
                TimelineHeader header = new TimelineHeader();
                header.render(new TimeScale(MON, 20), MON, MON.plusDays(7));
                String week = header.weekLabels().get(0).getText();
                assertTrue(week.contains("Jun"), "expected English month, got: " + week);
            });
        } finally {
            Locale.setDefault(system);
        }
    }

    /**
     * Button glyphs must come from this whitelist: ASCII plus symbols verified present in
     * Segoe UI (the app font). Anything outside renders as a tofu box on Windows.
     */
    private static final String SAFE_GLYPHS = "\u25ba\u25a0\u25c6\u2192\u2194";

    @Test
    void toolbarGlyphsRenderInSegoeUi() throws Exception {
        FxThread.run(() -> {
            SideToolBar bar = new SideToolBar(new com.richardl.wintak.controller.MainController());
            bar.getItems().forEach(item -> {
                if (item instanceof javafx.scene.control.ButtonBase button && button.getText() != null) {
                    for (char c : button.getText().toCharArray()) {
                        assertTrue(c < 128 || SAFE_GLYPHS.indexOf(c) >= 0,
                                "glyph '" + c + "' (U+" + Integer.toHexString(c)
                                        + ") in " + item.getId() + " is not Segoe UI-safe");
                    }
                }
            });
        });
    }

    @Test
    void theAppPinsEnglishAsTheDefaultLocale() {
        Locale system = Locale.getDefault();
        try {
            Locale.setDefault(Locale.CANADA_FRENCH);
            com.richardl.wintak.app.WintakApp.pinEnglishLocale();
            assertTrue(Locale.getDefault() == Locale.ENGLISH,
                    "startup must pin Locale.ENGLISH so built-in controls speak English");
        } finally {
            Locale.setDefault(system);
        }
    }
}
