package com.richardl.wintak.controller;

import com.richardl.wintak.model.GanttTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowTitleTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    @Test
    void cleanUntitledDocument() {
        MainController controller = new MainController();
        assertEquals("Wintak \u2014 Untitled", controller.windowTitleProperty().get());
    }

    @Test
    void dirtyAddsTheMarker() {
        MainController controller = new MainController();
        controller.getDocument().addTask(new GanttTask("t", DAY, 1));
        assertEquals("Wintak \u2014 Untitled \u2022", controller.windowTitleProperty().get());
    }

    @Test
    void savedFileShowsItsNameAndClearsTheMarker(@TempDir Path dir) {
        MainController controller = new MainController();
        Path target = dir.resolve("apollo.wintak.json");
        controller.setFileDialogs(new FileDialogs() {
            @Override
            public Optional<Path> chooseOpen() {
                return Optional.empty();
            }

            @Override
            public Optional<Path> chooseSaveTarget() {
                return Optional.of(target);
            }
        });
        controller.getDocument().addTask(new GanttTask("t", DAY, 1));

        controller.saveAs();
        assertEquals("Wintak \u2014 apollo.wintak.json", controller.windowTitleProperty().get());

        controller.getDocument().setTitle("edited");
        assertEquals("Wintak \u2014 apollo.wintak.json \u2022", controller.windowTitleProperty().get());
    }
}
