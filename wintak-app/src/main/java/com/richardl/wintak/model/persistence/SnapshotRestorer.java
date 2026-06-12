package com.richardl.wintak.model.persistence;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Snapshot -> live document. Pure; exact inverse of {@link SnapshotBuilder}. Preserves task
 * ids and ends with {@code markSaved()} so a load never looks like an edit.
 */
public final class SnapshotRestorer {

    private SnapshotRestorer() {
    }

    public static GanttDocument restore(DocumentSnapshot snapshot) {
        GanttDocument doc = new GanttDocument();
        doc.setTitle(snapshot.title());

        Map<String, GanttTask> byId = new HashMap<>();
        for (DocumentSnapshot.TaskSnapshot t : snapshot.tasks()) {
            GanttTask task = new GanttTask(t.id, t.name, LocalDate.parse(t.start), t.durationDays);
            task.setPercentComplete(t.percentComplete);
            task.setMilestone(t.milestone);
            doc.addTask(task);
            byId.put(task.getId(), task);
        }
        for (DocumentSnapshot.DependencySnapshot d : snapshot.dependencies()) {
            doc.addDependency(byId.get(d.predecessorId()), byId.get(d.successorId()));
        }

        doc.markSaved();
        return doc;
    }
}
