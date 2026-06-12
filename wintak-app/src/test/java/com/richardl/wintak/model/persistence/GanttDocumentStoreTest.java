package com.richardl.wintak.model.persistence;

import com.richardl.wintak.model.GanttDocument;
import com.richardl.wintak.model.GanttTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GanttDocumentStoreTest {

    private static final LocalDate DAY = LocalDate.of(2026, 6, 8);

    private final GanttDocumentStore store = new GanttDocumentStore();

    private static GanttDocument sampleDoc() {
        GanttDocument doc = new GanttDocument();
        doc.setTitle("Apollo");
        GanttTask design = new GanttTask("Design", DAY, 5);
        design.setPercentComplete(40);
        GanttTask build = new GanttTask("Build", DAY.plusDays(5), 10);
        doc.addTask(design);
        doc.addTask(build);
        doc.addDependency(design, build);
        return doc;
    }

    @Test
    void saveLoadRoundTripPreservesEverything(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("apollo.wintak.json");
        GanttDocument original = sampleDoc();
        store.save(original, file);

        GanttDocument loaded = store.load(file).orElseThrow();
        assertEquals("Apollo", loaded.getTitle());
        assertEquals(2, loaded.getTasks().size());

        GanttTask design = loaded.getTasks().get(0);
        assertEquals(original.getTasks().get(0).getId(), design.getId(), "ids must survive");
        assertEquals("Design", design.getName());
        assertEquals(DAY, design.getStart());
        assertEquals(5, design.getDurationDays());
        assertEquals(40, design.getPercentComplete());

        assertEquals(1, loaded.getDependencies().size());
        assertEquals(design, loaded.getDependencies().get(0).predecessor());
    }

    @Test
    void savingMarksTheDocumentCleanAndLoadingIsNotAnEdit(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("doc.json");
        GanttDocument original = sampleDoc();
        assertTrue(original.isDirty());
        store.save(original, file);
        assertFalse(original.isDirty(), "save establishes the clean baseline");

        GanttDocument loaded = store.load(file).orElseThrow();
        assertFalse(loaded.isDirty(), "a silent load must not look like an edit");
    }

    @Test
    void missingFileLoadsAsEmpty(@TempDir Path dir) throws IOException {
        assertTrue(store.load(dir.resolve("nope.json")).isEmpty());
    }

    @Test
    void corruptFileLoadsAsEmpty(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("garbage.json");
        Files.writeString(file, "{ not json !!!");
        Optional<GanttDocument> loaded = store.load(file);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void refusesAFileFromANewerSchema(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("future.json");
        Files.writeString(file, "{\"schemaVersion\": 999, \"title\": \"x\", \"tasks\": [], \"dependencies\": []}");
        assertThrows(IOException.class, () -> store.load(file));
    }

    @Test
    void milestoneFlagSurvivesRoundTrip(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("ms.wintak.json");
        GanttDocument doc = new GanttDocument();
        GanttTask ms = new GanttTask("Gate", DAY, 1);
        ms.setMilestone(true);
        doc.addTask(ms);
        store.save(doc, file);

        GanttDocument loaded = store.load(file).orElseThrow();
        assertTrue(loaded.getTasks().get(0).isMilestone(), "milestone flag must survive round-trip");
    }

    @Test
    void v1FileMigratesWithMilestoneFalse(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("v1.wintak.json");
        String v1Json = "{"
                + "\"schemaVersion\":1,"
                + "\"title\":\"Old\","
                + "\"tasks\":[{\"id\":\"1\",\"name\":\"Task\",\"start\":\"2026-06-08\","
                + "\"durationDays\":3,\"percentComplete\":0}],"
                + "\"dependencies\":[]"
                + "}";
        Files.writeString(file, v1Json);
        GanttDocument loaded = store.load(file).orElseThrow();
        assertFalse(loaded.getTasks().get(0).isMilestone(), "v1 tasks must default milestone=false");
    }

    @Test
    void missingRequiredFieldInTaskLoadsAsEmpty(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("bad.wintak.json");
        String json = "{"
                + "\"schemaVersion\":2,"
                + "\"title\":\"x\","
                + "\"tasks\":[{\"id\":\"1\",\"name\":\"Task\",\"durationDays\":3,\"percentComplete\":0}],"
                + "\"dependencies\":[]"
                + "}";
        Files.writeString(file, json);
        assertTrue(store.load(file).isEmpty(), "task with missing start must load as empty, not NPE");
    }

    @Test
    void saveReplacesTheTargetAtomically(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("doc.json");
        store.save(sampleDoc(), file);
        GanttDocument second = new GanttDocument();
        second.setTitle("Replacement");
        store.save(second, file);

        assertEquals("Replacement", store.load(file).orElseThrow().getTitle());
        try (var siblings = Files.list(dir)) {
            assertEquals(1, siblings.count(), "no temp files left behind");
        }
    }
}
