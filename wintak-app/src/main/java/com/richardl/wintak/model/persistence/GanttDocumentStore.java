package com.richardl.wintak.model.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richardl.wintak.model.GanttDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * File-level save/load of the Gantt document as JSON. Writes are atomic (temp file in the
 * same directory, then move); a missing or corrupt file loads as empty; a file stamped with
 * a newer schema than this build understands is refused outright.
 */
public class GanttDocumentStore {

    public static final int SCHEMA_VERSION = 2;

    private final ObjectMapper mapper = new ObjectMapper();

    public void save(GanttDocument doc, Path target) throws IOException {
        writeSnapshot(SnapshotBuilder.build(doc), target);
        doc.markSaved();
    }

    /**
     * Phase-1/phase-2 split for off-FX-thread writes. Call on the FX thread to snapshot the
     * document; invoke the returned {@link java.util.concurrent.Callable} on any thread to write
     * to disk (returns {@code null}). Caller is responsible for calling {@code doc.markSaved()}
     * on the FX thread after a successful write.
     */
    public java.util.concurrent.Callable<Void> beginSave(GanttDocument doc, Path target) {
        DocumentSnapshot snapshot = SnapshotBuilder.build(doc);
        return () -> { writeSnapshot(snapshot, target); return null; };
    }

    private void writeSnapshot(DocumentSnapshot snapshot, Path target) throws IOException {
        Path tmp = Files.createTempFile(target.toAbsolutePath().getParent(), target.getFileName().toString(), ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), snapshot);
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public Optional<GanttDocument> load(Path source) throws IOException {
        if (!Files.isRegularFile(source)) {
            return Optional.empty();
        }
        DocumentSnapshot snapshot;
        try {
            snapshot = mapper.readValue(source.toFile(), DocumentSnapshot.class);
        } catch (IOException corrupt) {
            return Optional.empty();
        }
        if (snapshot.schemaVersion() > SCHEMA_VERSION) {
            throw new IOException("file requires a newer Wintak (schema " + snapshot.schemaVersion()
                    + " > supported " + SCHEMA_VERSION + "): " + source);
        }
        try {
            return Optional.of(SnapshotRestorer.restore(snapshot));
        } catch (RuntimeException corrupt) {
            return Optional.empty();
        }
    }
}
