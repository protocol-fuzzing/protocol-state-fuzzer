package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Reads and writes tests from/to files.
 * <p>
 * Mutations of an input are encoded in the following way:
 * {@literal @} + input name + JSON encoding of the mutations.
 */
public class TestParser {

    /**
     * Writes test to file given the filename.
     *
     * @param test      the test to be written
     * @param filename  the name of the destination file
     *
     * @throws IOException  if an error during writing occurs
     */
    public void writeTest(Word<AbstractInput> test, String filename) throws IOException {
        writeTest(test, new File(filename));
    }

    /**
     * Writes test to file.
     *
     * @param test  the test to be written
     * @param file  the destination file
     *
     * @throws IOException  if an error during writing occurs
     */
    public void writeTest(Word<AbstractInput> test, File file) throws IOException {
        if (!file.createNewFile()) {
            throw new IOException("Unable to create file at specified path. It already exists");
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (AbstractInput input : test) {
                pw.println(input.toString());
            }
        }
    }

    /**
     * Reads a single test from file.
     *
     * @param alphabet  the alphabet of the test
     * @param filename  the name of the source file
     * @return          the test as a word of inputs
     *
     * @throws IOException  if an error during reading occurs
     */
    public Word<AbstractInput> readTest(Alphabet<AbstractInput> alphabet, String filename) throws IOException {
        return readTest(alphabet, parseTestFile(filename));
    }

    /**
     * Reads a single test from a list of input strings.
     *
     * @param alphabet          the alphabet of the test
     * @param testInputStrings  the list containing input strings
     * @return                  the test as a word of inputs
     */
    public Word<AbstractInput> readTest(Alphabet<AbstractInput> alphabet, List<String> testInputStrings) {
        Map<String, AbstractInput> inputs = new LinkedHashMap<>();
        alphabet.forEach(i -> inputs.put(i.toString(), i));

        Word<AbstractInput> inputWord = Word.epsilon();
        for (String inputString : testInputStrings) {
            inputString = inputString.trim();
            if (!inputs.containsKey(inputString)) {
                throw new RuntimeException("Input \"" + inputString + "\" is missing from the alphabet");
            }
            inputWord = inputWord.append(inputs.get(inputString));
        }

        return inputWord;
    }

    /**
     * Reads reset-separated tests from file.
     * <p>
     * It stops reading once it reaches EOF, or an empty (or blank) line.
     * A non-empty line may contain:
     * <ul>
     * <li> reset - marking the end of the current test, and the beginning of a new test
     * <li> space-separated regular inputs and resets
     * <li> a single mutated input (starts with @)
     * <li> commented line (starts with # or !)
     * </ul>
     *
     * @param alphabet  the alphabet of the tests
     * @param filename  the name of the source file
     * @return          the tests as a list of words of inputs, where each word
     *                  is a test specified in the source file
     *
     * @throws IOException  if an error during reading occurs
     */
    public List<Word<AbstractInput>> readTests(Alphabet<AbstractInput> alphabet, String filename) throws IOException {
        List<String> inputStrings = parseTestFile(filename);
        List<String> flattenedInputStrings = inputStrings.stream()
                .map(i -> i.startsWith("@") ? new String[]{i} : i.split("\\s+"))
                .flatMap(Arrays::stream)
                .toList();

        List<Word<AbstractInput>> tests = new ArrayList<>();
        List<String> currentTestStrings = new ArrayList<>();
        for (String inputString : flattenedInputStrings) {
            if (inputString.equals("reset")) {
                tests.add(readTest(alphabet, currentTestStrings));
                currentTestStrings.clear();
            } else {
                currentTestStrings.add(inputString);
            }
        }
        if (!inputStrings.isEmpty()) {
            tests.add(readTest(alphabet, currentTestStrings));
        }
        return tests;
    }

    /**
     * Parses the tests of a file into a String List.
     * <p>
     * Commented lines (starting with # or !) are ignored and not included
     * in the result and the parsing stops at the first empty/blank line.
     *
     * @param filename  the name of the source file
     * @return          the parsed and non-ignored lines of the file
     *
     * @throws IOException  if an error during reading occurs
     */
    protected List<String> parseTestFile(String filename) throws IOException {
        String line;
        List<String> trace = new ArrayList<>();

        try (BufferedReader bfr = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {
            while ((line = bfr.readLine()) != null) {

                // skip commented lines
                if (line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                // stop on first blank line (empty or whitespace only line)
                if (line.isBlank()) {
                    break;
                }

                trace.add(line + System.lineSeparator());
            }
        }

        return trace;
    }
}
