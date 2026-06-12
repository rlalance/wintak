package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import com.richardl.wintak.model.persistence.GanttDocumentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileCommandsTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @TempDir
    Path dir;

    private MainController controller;
    private StubDialogs dialogs;
    private StubPrompts prompts;

    private static final class StubDialogs implements FileDialogs {
        Path openTarget;
        Path saveTarget;

        @Override
        public Optional<Path> chooseOpen() {
            return Optional.ofNullable(openTarget);
        }

        @Override
        public Optional<Path> chooseSaveTarget() {
            return Optional.ofNullable(saveTarget);
        }
    }

    private static final class StubPrompts implements UserPrompts {
        SaveChoice answer = SaveChoice.DISCARD;
        final AtomicInteger asked = new AtomicInteger();

        @Override
        public SaveChoice confirmUnsaved() {
            asked.incrementAndGet();
            return answer;
        }

        @Override
        public void showError(String message) {
        }

        @Override
        public void showAbout() {
        }
    }

    @BeforeEach
    void setUp() {
        controller = new MainController();
        dialogs = new StubDialogs();
        prompts = new StubPrompts();
        controller.setFileDialogs(dialogs);
        controller.setUserPrompts(prompts);
    }

    private void dirtyTheDocument() {
        controller.getDocument().addTask(new GanttTask("t", DAY, 2));
    }

    @Test
    void saveAsWritesToTheChosenFileAndRemembersIt() throws IOException {
        dirtyTheDocument();
        dialogs.saveTarget = dir.resolve("plan.wintak.json");

        controller.saveAs();

        assertTrue(Files.isRegularFile(dialogs.saveTarget));
        assertEquals(dialogs.saveTarget, controller.getCurrentFile());
        assertFalse(controller.getDocument().isDirty());
    }

    @Test
    void saveWithAKnownFileSkipsTheDialog() throws IOException {
        dialogs.saveTarget = dir.resolve("plan.json");
        controller.saveAs();

        dialogs.saveTarget = null;
        dirtyTheDocument();
        controller.save();

        assertFalse(controller.getDocument().isDirty(), "saved straight to the known file");
    }

    @Test
    void saveWithNoFileFallsBackToSaveAs() {
        dirtyTheDocument();
        dialogs.saveTarget = dir.resolve("first.json");
        controller.save();
        assertEquals(dialogs.saveTarget, controller.getCurrentFile());
    }

    @Test
    void openLoadsTheChosenDocument() throws IOException {
        GanttDocument other = new GanttDocument();
        other.setTitle("Loaded");
        Path file = dir.resolve("other.json");
        new GanttDocumentStore().save(other, file);

        dialogs.openTarget = file;
        controller.open();

        assertEquals("Loaded", controller.getDocument().getTitle());
        assertEquals(file, controller.getCurrentFile());
        assertFalse(controller.getDocument().isDirty());
    }

    @Test
    void cancellingTheOpenDialogChangesNothing() {
        GanttDocument before = controller.getDocument();
        dialogs.openTarget = null;
        controller.open();
        assertSame(before, controller.getDocument());
    }

    @Test
    void cleanDocumentNeverPrompts() {
        controller.newDocument();
        controller.exit();
        assertEquals(0, prompts.asked.get());
    }

    @Test
    void dirtyNewPromptsAndCancelKeepsEverything() {
        dirtyTheDocument();
        GanttDocument before = controller.getDocument();
        prompts.answer = UserPrompts.SaveChoice.CANCEL;

        controller.newDocument();

        assertEquals(1, prompts.asked.get());
        assertSame(before, controller.getDocument());
    }

    @Test
    void dirtyNewWithDiscardReplacesTheDocument() {
        dirtyTheDocument();
        GanttDocument before = controller.getDocument();
        prompts.answer = UserPrompts.SaveChoice.DISCARD;

        controller.newDocument();

        assertNotSame(before, controller.getDocument());
        assertNull(controller.getCurrentFile(), "a new document has no file yet");
    }

    @Test
    void dirtyNewWithSaveSavesFirst() {
        dirtyTheDocument();
        prompts.answer = UserPrompts.SaveChoice.SAVE;
        dialogs.saveTarget = dir.resolve("rescued.json");

        controller.newDocument();

        assertTrue(Files.isRegularFile(dialogs.saveTarget), "document was rescued to disk");
        assertTrue(controller.getDocument().getTasks().isEmpty(), "then replaced");
    }

    @Test
    void dirtyExitHonoursCancel() {
        AtomicBoolean closed = new AtomicBoolean();
        controller.setOnCloseRequest(() -> closed.set(true));
        dirtyTheDocument();

        prompts.answer = UserPrompts.SaveChoice.CANCEL;
        controller.exit();
        assertFalse(closed.get());

        prompts.answer = UserPrompts.SaveChoice.DISCARD;
        controller.exit();
        assertTrue(closed.get());
    }
}
