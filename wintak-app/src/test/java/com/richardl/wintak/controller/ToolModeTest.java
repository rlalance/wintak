package com.richardl.wintak.controller;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolModeTest {

    @Test
    void theFiveEditingToolsExistInToolbarOrder() {
        assertEquals(List.of(ToolMode.SELECT, ToolMode.ADD_TASK, ToolMode.ADD_MILESTONE,
                ToolMode.LINK, ToolMode.PAN), List.of(ToolMode.values()));
    }

    @Test
    void controllerDefaultsToSelect() {
        assertSame(ToolMode.SELECT, new MainController().toolModeProperty().get());
    }

    @Test
    void activeToolIsObservableAndNeverNull() {
        MainController controller = new MainController();
        List<ToolMode> seen = new java.util.ArrayList<>();
        controller.toolModeProperty().addListener((obs, old, next) -> seen.add(next));

        controller.toolModeProperty().set(ToolMode.LINK);
        assertEquals(List.of(ToolMode.LINK), seen);

        assertThrows(NullPointerException.class, () -> controller.toolModeProperty().set(null));
        assertSame(ToolMode.LINK, controller.toolModeProperty().get());
    }
}
