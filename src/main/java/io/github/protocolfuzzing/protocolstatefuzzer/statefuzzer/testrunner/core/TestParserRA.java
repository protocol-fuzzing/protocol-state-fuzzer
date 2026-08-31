package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialization of {@link TestParserAbstract} for register automata,
 * where alphabets contain input symbols while tests contain instances of these symbols.
 */
public class TestParserRA extends TestParserAbstract<InputSymbol, PSymbolInstance> {

    /** Constructor. */
    public TestParserRA() {
        super();
    }

    /**
     * Reads a single test from a list of input strings.
     * Each string is parsed to produce a corresponding {@link PSymbolInstance}.
     * For more details, see {@link TestParserRA#deserialize(Map, String)}.
     *
     * @param  alphabet         the alphabet of input symbols
     * @param  testInputStrings the list containing input strings
     *
     * @return                  the test as a word of input symbol instances
     */
    @Override
    public Word<PSymbolInstance> readTest(Alphabet<InputSymbol> alphabet, List<String> testInputStrings) {
        Map<String, InputSymbol> inputs = new LinkedHashMap<>();
        alphabet.forEach(i -> inputs.put(i.getName(), i));

        Word<PSymbolInstance> inputWord = Word.epsilon();
        for (String inputString: testInputStrings) {
            PSymbolInstance testInput = deserialize(inputs, inputString);
            inputWord = inputWord.append(testInput);
        }

        return inputWord;
    }

    /**
     * Deserializes a PSymbolInstance instance.
     * <p>
     * Example:
     * </p>
     *
     * <pre>
     * Suppose an input symbol "Message" with two parameters.
     * "Message[0, 0.2]" results in symbol instance with 0 and 0.2 as values for the two parameters.
     * If "Message" has no parameters both "Message[]" and "Message" are accepted.
     * </pre>
     *
     * @param  symbolsByName            maps symbol names with input symbols
     * @param  value                    the serialized representation of a {@link PSymbolInstance}
     *
     * @return                          the deserialized PSymbolInstance
     *
     * @throws IllegalArgumentException if the representation is invalid
     */
    protected PSymbolInstance deserialize(Map<String, InputSymbol> symbolsByName, String value) {
        String name;
        String[] valueStrings = new String[0];
        int bracket = value.indexOf('[');

        if (bracket < 0) {
            name = value.trim();
        } else {
            if (!value.endsWith("]")) {
                throw new IllegalArgumentException(
                    "Invalid PSymbolInstance representation: " + value);
            }
            name = value.substring(0, bracket);
            String valuePart = value.substring(bracket + 1, value.length() - 1);
            if (!valuePart.isBlank()) {
                valueStrings = Arrays.stream(valuePart.split("\\s*,\\s*"))
                    .toArray(String[]::new);
            }
        }

        InputSymbol symbol = symbolsByName.get(name);
        if (symbol == null) {
            throw new IllegalArgumentException(
                "Could not find symbol %s in alphabet".formatted(name));
        }

        if (valueStrings.length != symbol.getPtypes().length) {
            throw new IllegalArgumentException(
                "Symbol %s has arity %d but %d values where given"
                    .formatted(symbol.getName(), symbol.getArity(), valueStrings.length));
        }

        DataValue[] values = new DataValue[valueStrings.length];
        for (int i = 0; i < valueStrings.length; i++) {
            values[i] = new DataValue(symbol.getPtypes()[i], new BigDecimal(valueStrings[i]));
        }

        return new PSymbolInstance(symbol, values);
    }
}
