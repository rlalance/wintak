package com.richardl.wintak.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    private GanttDocument doc;
    private GanttTask a;
    private GanttTask b;
    private GanttTask c;

    @BeforeEach
    void setUp() {
        doc = new GanttDocument();
        a = new GanttTask("a", DAY, 1);
        b = new GanttTask("b", DAY, 1);
        c = new GanttTask("c", DAY, 1);
        doc.addTask(a);
        doc.addTask(b);
        doc.addTask(c);
    }

    @Test
    void linksAreFinishToStartPairs() {
        doc.addDependency(a, b);
        assertEquals(List.of(new Dependency(a, b)), List.copyOf(doc.getDependencies()));
    }

    @Test
    void rejectsSelfLink() {
        assertThrows(IllegalArgumentException.class, () -> doc.addDependency(a, a));
    }

    @Test
    void rejectsDuplicateLink() {
        doc.addDependency(a, b);
        assertThrows(IllegalArgumentException.class, () -> doc.addDependency(a, b));
    }

    @Test
    void rejectsLinkToTaskOutsideTheDocument() {
        GanttTask stranger = new GanttTask("x", DAY, 1);
        assertThrows(IllegalArgumentException.class, () -> doc.addDependency(a, stranger));
    }

    @Test
    void rejectsDirectAndTransitiveCycles() {
        doc.addDependency(a, b);
        assertThrows(IllegalArgumentException.class, () -> doc.addDependency(b, a));
        doc.addDependency(b, c);
        assertThrows(IllegalArgumentException.class, () -> doc.addDependency(c, a));
    }

    @Test
    void removingATaskDropsItsLinks() {
        doc.addDependency(a, b);
        doc.addDependency(b, c);
        doc.removeTask(b);
        assertTrue(doc.getDependencies().isEmpty());
    }

    @Test
    void linksCanBeRemoved() {
        doc.addDependency(a, b);
        doc.removeDependency(a, b);
        assertTrue(doc.getDependencies().isEmpty());
        // and the reverse link is legal again once the cycle risk is gone
        doc.addDependency(b, a);
        assertEquals(1, doc.getDependencies().size());
    }
}
