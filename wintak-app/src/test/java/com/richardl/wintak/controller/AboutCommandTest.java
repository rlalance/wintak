package com.richardl.wintak.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AboutCommandTest {

    @Test
    void aboutInvokesTheSeam() {
        boolean[] called = { false };
        MainController controller = new MainController();
        controller.setUserPrompts(new UserPrompts() {
            @Override public UserPrompts.SaveChoice confirmUnsaved() { return SaveChoice.DISCARD; }
            @Override public void showError(String message) { }
            @Override public void showAbout() { called[0] = true; }
        });
        controller.about();
        assertTrue(called[0], "about() must delegate to UserPrompts.showAbout()");
    }
}
