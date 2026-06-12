package com.richardl.wintak;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AsciiSourceTest {

    @Test
    void javaSourcesArePureAscii() throws IOException {
        List<String> offenders;
        try (Stream<Path> sources = Files.walk(Path.of("src"))) {
            offenders = sources
                    .filter(p -> p.toString().endsWith(".java"))
                    .flatMap(AsciiSourceTest::nonAsciiLines)
                    .toList();
        }
        assertTrue(offenders.isEmpty(),
                "non-ASCII characters in Java sources (use \\uXXXX escapes):\n"
                        + String.join("\n", offenders));
    }

    private static Stream<String> nonAsciiLines(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return java.util.stream.IntStream.range(0, lines.size())
                    .filter(i -> lines.get(i).chars().anyMatch(c -> c > 127))
                    .mapToObj(i -> file + ":" + (i + 1))
                    .toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
