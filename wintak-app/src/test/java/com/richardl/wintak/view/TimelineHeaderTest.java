package com.richardl.wintak.view;

import com.richardl.wintak.model.TimeScale;
import com.richardl.wintak.testutil.FxThread;
import com.richardl.wintak.testutil.FxToolkitExtension;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(FxToolkitExtension.class)
class TimelineHeaderTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8); // a Monday

    @Test
    void weekLabelsSitAtTheirTickPositions() throws Exception {
        FxThread.run(() -> {
            TimelineHeader header = new TimelineHeader();
            TimeScale scale = new TimeScale(MON, 20);
            header.render(scale, MON, MON.plusDays(20));

            List<Label> weeks = header.weekLabels();
            assertFalse(weeks.isEmpty());
            assertEquals(scale.xOf(MON), weeks.get(0).getLayoutX());
            assertEquals(scale.xOf(MON.plusWeeks(1)), weeks.get(1).getLayoutX());
            assertTrue(weeks.get(0).getText().contains("8"), "week label shows the date");
        });
    }

    @Test
    void dayLabelsAppearOnlyAtHighZoom() throws Exception {
        FxThread.run(() -> {
            TimelineHeader header = new TimelineHeader();
            header.render(new TimeScale(MON, 20), MON, MON.plusDays(7));
            assertFalse(header.dayLabels().isEmpty(), "20 px/day is wide enough for day labels");

            header.render(new TimeScale(MON, 5), MON, MON.plusDays(7));
            assertTrue(header.dayLabels().isEmpty(), "5 px/day is too dense for day labels");
        });
    }

    @Test
    void reRenderingReplacesTheOldLabels() throws Exception {
        FxThread.run(() -> {
            TimelineHeader header = new TimelineHeader();
            TimeScale scale = new TimeScale(MON, 20);
            header.render(scale, MON, MON.plusDays(7));
            int firstCount = header.getChildren().size();
            header.render(scale, MON, MON.plusDays(7));
            assertEquals(firstCount, header.getChildren().size(), "render must be idempotent");
        });
    }
}
