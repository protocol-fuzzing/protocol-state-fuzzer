package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialization of {@link TestParserAbstract} for when real and transformed inputs are the same type.
 *
 * @param <I> the type of input symbols contained in the alphabet and in words
 */
public class TestParserStandard<I> extends TestParserAbstract<I, I> {

    /** Constructor. */
    public TestParserStandard() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Word<I> readTest(Alphabet<I> alphabet, List<String> testInputStrings) {
        Map<String, I> inputsByName = new LinkedHashMap<>();
        alphabet.forEach(i -> inputsByName.put(i.toString(), i));

        Word<I> inputWord = Word.epsilon();
        for (String inputString: testInputStrings) {
            I input = inputsByName.get(inputString);
            if (input == null) {
                throw new IllegalArgumentException("Could not find symbol %s in alphabet".formatted(inputString));
            }
            inputWord = inputWord.append(input);
        }

        return inputWord;
    }

}
