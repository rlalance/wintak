package com.richardl.wintak.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GanttDocumentTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    private static GanttTask task(String name) {
        return new GanttTask(name, DAY, 1);
    }

    @Test
    void newDocumentHasDefaultTitleAndNoTasks() {
        GanttDocument doc = new GanttDocument();
        assertEquals("Untitled project", doc.getTitle());
        assertTrue(doc.getTasks().isEmpty());
    }

    @Test
    void addRemoveAndReorderKeepOrder() {
        GanttDocument doc = new GanttDocument();
        GanttTask a = task("a");
        GanttTask b = task("b");
        GanttTask c = task("c");
        doc.addTask(a);
        doc.addTask(b);
        doc.addTask(c);
        assertEquals(List.of(a, b, c), List.copyOf(doc.getTasks()));

        doc.moveTask(2, 0);
        assertEquals(List.of(c, a, b), List.copyOf(doc.getTasks()));

        doc.removeTask(a);
        assertEquals(List.of(c, b), List.copyOf(doc.getTasks()));
    }

    @Test
    void rejectsDuplicateTaskInstance() {
        GanttDocument doc = new GanttDocument();
        GanttTask a = task("a");
        doc.addTask(a);
        assertThrows(IllegalArgumentException.class, () -> doc.addTask(a));
    }

    @Test
    void taskListIsObservableForViewBinding() {
        GanttDocument doc = new GanttDocument();
        AtomicInteger changes = new AtomicInteger();
        doc.getTasks().addListener((javafx.collections.ListChangeListener<GanttTask>) c -> changes.incrementAndGet());
        doc.addTask(task("a"));
        assertEquals(1, changes.get());
    }

    @Test
    void titleIsAnObservableProperty() {
        GanttDocument doc = new GanttDocument();
        doc.setTitle("Apollo");
        assertEquals("Apollo", doc.titleProperty().get());
    }
}
