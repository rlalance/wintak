package com.richardl.wintak.controller;

/** Seam around blocking user prompts (Alert) so commands are testable with stubs. */
public interface UserPrompts {

    enum SaveChoice { SAVE, DISCARD, CANCEL }

    /** "You have unsaved changes" - what does the user want to do? */
    SaveChoice confirmUnsaved();

    void showError(String message);

    void showAbout();

    /** Null object: never blocks - discards changes silently and swallows errors. */
    UserPrompts NONE = new UserPrompts() {
        @Override
        public SaveChoice confirmUnsaved() {
            return SaveChoice.DISCARD;
        }

        @Override
        public void showError(String message) {
        }

        @Override
        public void showAbout() {
        }
    };
}
