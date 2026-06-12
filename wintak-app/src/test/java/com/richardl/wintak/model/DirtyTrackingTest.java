package com.richardl.wintak.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirtyTrackingTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    private GanttDocument doc;

    @BeforeEach
    void setUp() {
        doc = new GanttDocument();
    }

    @Test
    void freshDocumentIsClean() {
        assertFalse(doc.isDirty());
    }

    @Test
    void titleChangeMarksDirty() {
        doc.setTitle("Apollo");
        assertTrue(doc.isDirty());
    }

    @Test
    void taskAndDependencyMutationsMarkDirty() {
        GanttTask a = new GanttTask("a", DAY, 1);
        GanttTask b = new GanttTask("b", DAY, 1);

        doc.addTask(a);
        assertTrue(doc.isDirty());
        doc.markSaved();

        doc.addTask(b);
        doc.markSaved();

        doc.addDependency(a, b);
        assertTrue(doc.isDirty());
        doc.markSaved();

        doc.removeTask(b);
        assertTrue(doc.isDirty());
    }

    @Test
    void editingATasksFieldsMarksDirty() {
        GanttTask a = new GanttTask("a", DAY, 1);
        doc.addTask(a);
        doc.markSaved();

        a.setName("renamed");
        assertTrue(doc.isDirty());
        doc.markSaved();

        a.setDurationDays(4);
        assertTrue(doc.isDirty());
        doc.markSaved();

        a.setPercentComplete(50);
        assertTrue(doc.isDirty());
    }

    @Test
    void editingARemovedTaskDoesNotMarkDirty() {
        GanttTask a = new GanttTask("a", DAY, 1);
        doc.addTask(a);
        doc.removeTask(a);
        doc.markSaved();

        a.setName("ghost edit");
        assertFalse(doc.isDirty());
    }

    @Test
    void markSavedClearsAndPropertyIsObservable() {
        var seen = new java.util.ArrayList<Boolean>();
        doc.dirtyProperty().addListener((obs, old, next) -> seen.add(next));
        doc.setTitle("x");
        doc.markSaved();
        assertFalse(doc.isDirty());
        assertTrue(seen.contains(Boolean.TRUE) && seen.contains(Boolean.FALSE));
    }
}
