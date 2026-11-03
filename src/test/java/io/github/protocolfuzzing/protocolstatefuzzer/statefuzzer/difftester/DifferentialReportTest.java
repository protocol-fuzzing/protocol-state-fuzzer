package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DifferentialReportTest {

    @TempDir
    Path tempDir;

    private DifferentialReport<String, String> buildReport() {
        Path testFilePath = tempDir.resolve("witnesses.txt");
        Path reportPath = tempDir.resolve("report.txt");
        return new DifferentialReport<>(testFilePath, reportPath);
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
        DivergenceRecord<String, String> d = new DivergenceRecord<>(List.of("CLIENT_HELLO", "FINISHED"), "A", "B");

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
        DivergenceRecord<String, String> d1 = new DivergenceRecord<>(List.of("CLIENT_HELLO", "FINISHED"), "A", "B");
        DivergenceRecord<String, String> d2 = new DivergenceRecord<>(List.of("CLIENT_HELLO", "CLIENT_HELLO",
            "FINISHED"), "A", "B");

        DifferentialReport<String, String> report = buildReport();
        report.writeTestFile(List.of(d1, d2));

        Path testFile = tempDir.resolve("witnesses.txt");
        List<String> lines = Files.readAllLines(testFile);

        long commentCount = lines.stream().filter(l -> l.startsWith("#")).count();
        assertEquals(4, commentCount);
    }

    @Test
    public void writeReport_correctNumerOfDivergences() throws IOException {
        DivergenceRecord<String, String> d1 = new DivergenceRecord<>(List.of("CLIENT_HELLO", "FINISHED"), "A", "B");
        DivergenceRecord<String, String> d2 = new DivergenceRecord<>(List.of("CLIENT_HELLO", "CLIENT_HELLO",
            "FINISHED"), "A", "B");

        DifferentialReport<String, String> report = buildReport();
        report.writeReport(List.of(d1, d2), "ModelA.dot", "ModelB.dot");

        Path reportFile = tempDir.resolve("report.txt");
        List<String> lines = Files.readAllLines(reportFile);

        long divergenceHeaders = lines.stream().filter(l -> l.startsWith("Divergence")).count();
        assertEquals(2, divergenceHeaders);
    }
}
