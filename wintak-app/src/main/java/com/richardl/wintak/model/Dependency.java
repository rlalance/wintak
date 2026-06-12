package com.richardl.wintak.model;

import java.util.Objects;

/** A finish-to-start link: {@code successor} may not start before {@code predecessor} finishes. */
public record Dependency(GanttTask predecessor, GanttTask successor) {

    public Dependency {
        Objects.requireNonNull(predecessor, "predecessor");
        Objects.requireNonNull(successor, "successor");
        if (predecessor == successor) {
            throw new IllegalArgumentException("a task cannot depend on itself: " + predecessor);
        }
    }
}
