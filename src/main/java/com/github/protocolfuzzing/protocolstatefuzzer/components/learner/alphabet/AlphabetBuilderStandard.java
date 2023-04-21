package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.AlphabetOptionProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import net.automatalib.words.Alphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The standard implementation of the AlphabetBuilder that requires a
 * file specific AlphabetSerializer.
 */
public class AlphabetBuilderStandard implements AlphabetBuilder {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Stores the name with the extension provided by {@link #alphabetSerializer}
     * of the default alphabet file that should be in resources.
     */
    protected String DEFAULT_ALPHABET;

    /** Stores the constructor parameter. */
    protected AlphabetSerializer alphabetSerializer;

    /** Stores already built alphabets so as not to rebuild them if needed. */
    protected Map<AlphabetOptionProvider, Alphabet<AbstractInput>> alphabetMap = new LinkedHashMap<>();

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alphabetSerializer  the AlphabetSerializer to be used
     */
    public AlphabetBuilderStandard(AlphabetSerializer alphabetSerializer) {
        this.alphabetSerializer = alphabetSerializer;
        this.DEFAULT_ALPHABET = DEFAULT_ALPHABET_NO_EXTENSION + alphabetSerializer.getAlphabetFileExtension();
    }

    @Override
    public Alphabet<AbstractInput> build(AlphabetOptionProvider alphabetProvider) {
        if (alphabetMap.containsKey(alphabetProvider)) {
            return alphabetMap.get(alphabetProvider);
        }

        Alphabet<AbstractInput> alphabet = null;
        if (alphabetProvider.getAlphabet() != null) {
            // read provided alphabet
            try (InputStream inputStream = getAlphabetFileInputStream(alphabetProvider)) {
                alphabet = alphabetSerializer.read(inputStream);
            } catch (AlphabetSerializerException e) {
                LOGGER.fatal("Failed to instantiate provided alphabet");
                throw new RuntimeException(e);
            } catch (IOException e) {
                LOGGER.debug("Failed to close input stream of provided alphabet");
            }
        } else {
            // read default alphabet
            try (InputStream inputStream = getAlphabetFileInputStream(alphabetProvider)) {
                alphabet = alphabetSerializer.read(getAlphabetFileInputStream(null));
            } catch (AlphabetSerializerException e) {
                LOGGER.fatal("Failed to instantiate default alphabet");
                throw new RuntimeException(e);
            } catch (IOException e) {
                LOGGER.debug("Failed to close input stream of default alphabet");
            }
        }

        alphabetMap.put(alphabetProvider, alphabet);
        return alphabet;
    }

    @Override
    public InputStream getAlphabetFileInputStream(AlphabetOptionProvider alphabetProvider) {
        if (alphabetProvider == null || alphabetProvider.getAlphabet() == null) {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_ALPHABET);

            if (stream == null) {
                String msg = "Failed to find the default alphabet file: " + DEFAULT_ALPHABET;
                LOGGER.fatal(msg);
                throw new RuntimeException(msg);
            }

            return stream;
        }

        try {
            return new FileInputStream(alphabetProvider.getAlphabet());
        } catch (FileNotFoundException e) {
            LOGGER.fatal("Failed to find the provided alphabet file: {}", alphabetProvider.getAlphabet());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAlphabetFileExtension() {
        return alphabetSerializer.getAlphabetFileExtension();
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<AbstractInput> alphabet)
        throws IOException, AlphabetSerializerException {

        try (FileOutputStream alphabetStream = new FileOutputStream(outputFileName)) {
            alphabetSerializer.write(alphabetStream, alphabet);
        }
    }
}
