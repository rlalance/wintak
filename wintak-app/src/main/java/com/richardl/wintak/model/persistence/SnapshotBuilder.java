package com.richardl.wintak.model.persistence;

import com.richardl.wintak.model.GanttDocument;

/** Live document -> serialisable snapshot. Pure; exact inverse of {@link SnapshotRestorer}. */
public final class SnapshotBuilder {

    private SnapshotBuilder() {
    }

    public static DocumentSnapshot build(GanttDocument doc) {
        return new DocumentSnapshot(
                GanttDocumentStore.SCHEMA_VERSION,
                doc.getTitle(),
                doc.getTasks().stream()
                        .map(t -> {
                            DocumentSnapshot.TaskSnapshot ts = new DocumentSnapshot.TaskSnapshot();
                            ts.id = t.getId();
                            ts.name = t.getName();
                            ts.start = t.getStart().toString();
                            ts.durationDays = t.getDurationDays();
                            ts.percentComplete = t.getPercentComplete();
                            ts.milestone = t.isMilestone();
                            return ts;
                        })
                        .toList(),
                doc.getDependencies().stream()
                        .map(d -> new DocumentSnapshot.DependencySnapshot(
                                d.predecessor().getId(), d.successor().getId()))
                        .toList());
    }
}
