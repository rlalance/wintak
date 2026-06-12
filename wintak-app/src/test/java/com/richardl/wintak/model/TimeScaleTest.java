package com.richardl.wintak.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeScaleTest {

    private static final LocalDate MON = LocalDate.of(2026, 6, 8); // a Monday

    private final TimeScale scale = new TimeScale(MON, 20);

    @Test
    void originMapsToZeroAndDaysScaleLinearly() {
        assertEquals(0, scale.xOf(MON));
        assertEquals(20, scale.xOf(MON.plusDays(1)));
        assertEquals(140, scale.xOf(MON.plusWeeks(1)));
        assertEquals(-20, scale.xOf(MON.minusDays(1)), "dates before the origin go negative");
    }

    @Test
    void dateAtIsTheInverseOfXOf() {
        for (int day = -10; day <= 10; day++) {
            LocalDate date = MON.plusDays(day);
            assertEquals(date, scale.dateAt(scale.xOf(date)));
        }
    }

    @Test
    void dateAtSnapsWithinTheDayCell() {
        assertEquals(MON, scale.dateAt(0.0));
        assertEquals(MON, scale.dateAt(19.9));
        assertEquals(MON.plusDays(1), scale.dateAt(20.0));
        assertEquals(MON.minusDays(1), scale.dateAt(-0.1));
    }

    @Test
    void rejectsNonPositiveZoom() {
        assertThrows(IllegalArgumentException.class, () -> new TimeScale(MON, 0));
        assertThrows(IllegalArgumentException.class, () -> new TimeScale(MON, -5));
    }

    @Test
    void dayTicksCoverTheRangeInclusive() {
        List<LocalDate> ticks = scale.ticks(TimeScale.TickUnit.DAY, MON, MON.plusDays(3));
        assertEquals(List.of(MON, MON.plusDays(1), MON.plusDays(2), MON.plusDays(3)), ticks);
    }

    @Test
    void weekTicksFallOnMondaysAndCoverTheRange() {
        List<LocalDate> ticks = scale.ticks(TimeScale.TickUnit.WEEK, MON.plusDays(2), MON.plusDays(20));
        assertTrue(ticks.stream().allMatch(t -> t.getDayOfWeek() == DayOfWeek.MONDAY));
        assertEquals(MON, ticks.get(0), "first tick aligns backwards to cover the range start");
        assertTrue(ticks.get(ticks.size() - 1).isAfter(MON.plusDays(20)) ||
                ticks.get(ticks.size() - 1).equals(MON.plusDays(20)));
    }

    @Test
    void monthTicksFallOnTheFirst() {
        List<LocalDate> ticks = scale.ticks(TimeScale.TickUnit.MONTH,
                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 8, 10));
        assertEquals(List.of(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1)), ticks);
    }
}
