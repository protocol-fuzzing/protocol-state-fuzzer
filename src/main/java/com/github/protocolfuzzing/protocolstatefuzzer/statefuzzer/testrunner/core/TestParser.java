package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Reads tests from a file and writes them to a file using an alphabet.
 * <p>
 * Mutations of an input are encoded in the following way: {@literal @} + input
 * name + JSON encoding of the mutations.
 */

public class TestParser {
    private static final Logger LOGGER = LogManager.getLogger();

    public void writeTest(Word<AbstractInput> test, String PATH) throws IOException {
        File file = new File(PATH);
        writeTest(test, file);
    }

    public void writeTest(Word<AbstractInput> test, File file) throws IOException {
        file.createNewFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (AbstractInput input : test) {
                pw.println(input.toString());
            }
        }
    }

    public Word<AbstractInput> readTest(Alphabet<AbstractInput> alphabet, String PATH) throws IOException {
        List<String> inputStrings = readTestStrings(PATH);
        return readTest(alphabet, inputStrings);
    }

    public Word<AbstractInput> readTest(Alphabet<AbstractInput> alphabet, List<String> testInputStrings) {
        Map<String, AbstractInput> inputs = new LinkedHashMap<>();
        alphabet.forEach(i -> inputs.put(i.toString(), i));
        Word<AbstractInput> inputWord = Word.epsilon();
        for (String inputString : testInputStrings) {
            inputString = inputString.trim();
            if (!inputs.containsKey(inputString)) {
                throw new RuntimeException("Input \"" + inputString + "\" is missing from the alphabet ");
            }
            inputWord = inputWord.append(inputs.get(inputString));
        }

        return inputWord;
    }

    /**
     * Reads from a file reset-separated test queries. It stops reading once it
     * reaches the EOF, or an empty line. A non-empty line may contain:
     * <ul>
     * <li>reset - marking the end of the current test, and the beginning of a new
     * test</li>
     * <li>space-separated regular inputs and resets</li>
     * <li>a single mutated input (starts with @)</li>
     * <li>commented line (starts with # or !)</li>
     * </ul>
     */
    public List<Word<AbstractInput>> readTests(Alphabet<AbstractInput> alphabet, String PATH) throws IOException {
        List<String> inputStrings = readTestStrings(PATH);
        List<String> flattenedInputStrings = inputStrings.stream()
                .map(i -> i.startsWith("@") ? new String[]{i} : i.split("\\s+"))
                .flatMap(Arrays::stream)
                .toList();

        List<Word<AbstractInput>> tests = new LinkedList<>();
        LinkedList<String> currentTestStrings = new LinkedList<>();
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

    protected List<String> readTestStrings(String PATH) throws IOException {
        List<String> trace;
        trace = Files.readAllLines(Paths.get(PATH), StandardCharsets.US_ASCII);
        ListIterator<String> it = trace.listIterator();
        while (it.hasNext()) {
            String line = it.next();
            if (line.startsWith("#") || line.startsWith("!")) {
                it.remove();
            } else {
                if (line.isEmpty()) {
                    it.remove();
                    while (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                }
            }
        }
        return trace;
    }
}
