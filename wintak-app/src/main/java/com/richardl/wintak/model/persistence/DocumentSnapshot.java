package com.richardl.wintak.model.persistence;

import java.util.List;

/**
 * Serialisable form of a document - plain values only, no JavaFX types. Dates are ISO-8601
 * strings so the JSON stays library-portable. {@code schemaVersion} gates loading (see
 * {@link GanttDocumentStore#SCHEMA_VERSION}).
 */
public record DocumentSnapshot(
        int schemaVersion,
        String title,
        List<TaskSnapshot> tasks,
        List<DependencySnapshot> dependencies) {

    /** Mutable class (not a record) so Jackson can set fields absent in v1 JSON to their defaults. */
    public static class TaskSnapshot {
        public String id;
        public String name;
        public String start;
        public int durationDays;
        public int percentComplete;
        public boolean milestone = false; // v2; missing in v1 files -> defaults false
    }

    public record DependencySnapshot(String predecessorId, String successorId) {
    }
}
