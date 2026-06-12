package com.richardl.wintak.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GanttTaskTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 6, 8);

    @Test
    void carriesIdNameStartDurationAndProgress() {
        GanttTask task = new GanttTask("Design hull", MONDAY, 5);
        assertNotNull(task.getId());
        assertEquals("Design hull", task.getName());
        assertEquals(MONDAY, task.getStart());
        assertEquals(5, task.getDurationDays());
        assertEquals(0, task.getPercentComplete());
    }

    @Test
    void idsAreUniquePerTask() {
        assertNotEquals(new GanttTask("a", MONDAY, 1).getId(),
                new GanttTask("a", MONDAY, 1).getId());
    }

    @Test
    void endDateIsStartPlusDuration() {
        GanttTask task = new GanttTask("t", MONDAY, 5);
        assertEquals(MONDAY.plusDays(5), task.getEndExclusive());
    }

    @Test
    void rejectsEmptyOrBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new GanttTask("", MONDAY, 1));
        assertThrows(IllegalArgumentException.class, () -> new GanttTask("   ", MONDAY, 1));
        GanttTask task = new GanttTask("ok", MONDAY, 1);
        assertThrows(IllegalArgumentException.class, () -> task.setName(" "));
        assertEquals("ok", task.getName());
    }

    @Test
    void rejectsNonPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> new GanttTask("t", MONDAY, 0));
        GanttTask task = new GanttTask("t", MONDAY, 3);
        assertThrows(IllegalArgumentException.class, () -> task.setDurationDays(-1));
        assertEquals(3, task.getDurationDays());
    }

    @Test
    void rejectsProgressOutsideZeroToHundred() {
        GanttTask task = new GanttTask("t", MONDAY, 1);
        assertThrows(IllegalArgumentException.class, () -> task.setPercentComplete(-1));
        assertThrows(IllegalArgumentException.class, () -> task.setPercentComplete(101));
        task.setPercentComplete(100);
        assertEquals(100, task.getPercentComplete());
    }

    @Test
    void propertiesAreObservable() {
        GanttTask task = new GanttTask("t", MONDAY, 1);
        StringBuilder seen = new StringBuilder();
        task.nameProperty().addListener((obs, old, next) -> seen.append(next));
        task.setName("renamed");
        assertEquals("renamed", seen.toString());
    }

    @Test
    void milestoneDefaultsFalse() {
        assertFalse(new GanttTask("t", MONDAY, 1).isMilestone());
    }

    @Test
    void milestoneIsSettableAndObservable() {
        GanttTask task = new GanttTask("t", MONDAY, 1);
        boolean[] fired = { false };
        task.milestoneProperty().addListener((obs, old, next) -> fired[0] = next);
        task.setMilestone(true);
        assertTrue(task.isMilestone());
        assertTrue(fired[0], "milestone property change must fire a listener");
    }

    @Test
    void milestoneDurationIsLockedAtOne() {
        GanttTask task = new GanttTask("t", MONDAY, 1);
        task.setMilestone(true);
        assertThrows(IllegalArgumentException.class, () -> task.setDurationDays(5),
                "setDurationDays must be rejected on a milestone");
        assertEquals(1, task.getDurationDays(), "duration must stay at 1 after rejection");
    }

    @Test
    void setMilestoneThrowsIfDurationIsNotOne() {
        GanttTask task = new GanttTask("t", MONDAY, 5);
        assertThrows(IllegalArgumentException.class, () -> task.setMilestone(true),
                "setMilestone(true) must be rejected when durationDays != 1");
        assertFalse(task.isMilestone(), "milestone must remain false after rejection");
    }
}
