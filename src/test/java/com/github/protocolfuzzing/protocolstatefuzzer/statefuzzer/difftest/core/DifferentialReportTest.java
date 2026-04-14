package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DifferentialReportTest {

    private Path tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("difftest");
    }

    @After
    public void tearDown() throws IOException {
        try (Stream<Path> stream = Files.walk(tempDir)) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private DifferentialReport<String, String> buildReport() {
        Path testFilePath = tempDir.resolve("witnesses.txt");
        return new DifferentialReport<>(testFilePath);
    }

    @Test
    public void noDivergence_TestFileEmpty() throws IOException {
        DifferentialReport<String, String> report = buildReport();
        report.writeTestFile(List.of());

        Path testFile = tempDir.resolve("witnesses.txt");
        String content = Files.readString(testFile);

        assertTrue(content.isBlank());
    }

    @Test
    public void singleDivergence_correctFormat() throws IOException {
        DivergenceRecord<String, String> d = new DivergenceRecord<>(List.of("CLIENT_HELLO"), "FINISHED", "A", "B");

        DifferentialReport<String, String> report = buildReport();
        report.writeTestFile(List.of(d));

        Path testFile = tempDir.resolve("witnesses.txt");
        List<String> lines = Files.readAllLines(testFile);

        List<String> contentLines = lines.stream().filter(l -> !l.startsWith("#")).collect(Collectors.toList());

        assertEquals("CLIENT_HELLO", contentLines.get(0));
        assertEquals("FINISHED", contentLines.get(1));

    }

    @Test
    public void multipleDivergnces_correctNumberOfSequences() throws IOException {
        DivergenceRecord<String, String> d1 = new DivergenceRecord<>(List.of("CLIENT_HELLO"), "FINISHED", "A", "B");
        DivergenceRecord<String, String> d2 = new DivergenceRecord<>(List.of("CLIENT_HELLO", "CLIENT_HELLO"),
            "FINISHED", "A", "B");

        DifferentialReport<String, String> report = buildReport();
        report.writeTestFile(List.of(d1, d2));

        Path testFile = tempDir.resolve("witnesses.txt");
        List<String> lines = Files.readAllLines(testFile);

        long commentCount = lines.stream().filter(l -> l.startsWith("#")).count();
        assertEquals(4, commentCount);
    }
}
