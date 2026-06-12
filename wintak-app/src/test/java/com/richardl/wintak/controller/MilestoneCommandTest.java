package com.richardl.wintak.controller;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilestoneCommandTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void addMilestoneAtCreatesAMilestoneTask() {
        MainController controller = new MainController();
        controller.addMilestoneAt(DAY);

        assertEquals(1, controller.getDocument().getTasks().size());
        var task = controller.getDocument().getTasks().get(0);
        assertTrue(task.isMilestone(), "task must be marked as a milestone");
        assertEquals(DAY, task.getStart());
        assertEquals(1, task.getDurationDays(), "milestones are locked at 1 day");
    }

    @Test
    void addMilestoneIsUndoable() {
        MainController controller = new MainController();
        controller.addMilestoneAt(DAY);
        assertEquals(1, controller.getDocument().getTasks().size());

        controller.undo();
        assertEquals(0, controller.getDocument().getTasks().size(), "undo must remove the milestone");

        controller.redo();
        assertEquals(1, controller.getDocument().getTasks().size(), "redo must restore the milestone");
    }
}
