package com.richardl.wintak.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure timeline geometry: maps dates to x-pixels for a given origin and zoom (px/day) and
 * generates aligned ruler ticks. Every piece of chart math goes through here so it stays
 * unit-testable.
 */
public record TimeScale(LocalDate origin, double pxPerDay) {

    public enum TickUnit { DAY, WEEK, MONTH }

    public TimeScale {
        Objects.requireNonNull(origin, "origin");
        if (pxPerDay <= 0) {
            throw new IllegalArgumentException("pxPerDay must be > 0, was " + pxPerDay);
        }
    }

    /** X of the start of {@code date}'s day cell; negative left of the origin. */
    public double xOf(LocalDate date) {
        return ChronoUnit.DAYS.between(origin, date) * pxPerDay;
    }

    /** The date whose day cell contains {@code x}; exact inverse of {@link #xOf}. */
    public LocalDate dateAt(double x) {
        return origin.plusDays((long) Math.floor(x / pxPerDay));
    }

    /**
     * Aligned ticks covering {@code [from, to]} inclusively: DAY ticks on every day, WEEK
     * ticks on Mondays, MONTH ticks on the 1st - the first tick aligns backwards so the
     * range start is always covered, the last one closes the range.
     */
    public List<LocalDate> ticks(TickUnit unit, LocalDate from, LocalDate to) {
        LocalDate tick = align(unit, from);
        List<LocalDate> ticks = new ArrayList<>();
        while (tick.isBefore(to)) {
            ticks.add(tick);
            tick = next(unit, tick);
        }
        ticks.add(tick);
        return ticks;
    }

    private static LocalDate align(TickUnit unit, LocalDate date) {
        return switch (unit) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    private static LocalDate next(TickUnit unit, LocalDate tick) {
        return switch (unit) {
            case DAY -> tick.plusDays(1);
            case WEEK -> tick.plusWeeks(1);
            case MONTH -> tick.plusMonths(1);
        };
    }
}
