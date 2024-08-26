package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;
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
 *
 * @param <I>  the type of inputs
 */
public class AlphabetBuilderStandard<I> implements AlphabetBuilder<I> {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Stores the name with the extension provided by {@link #alphabetSerializer}
     * of the default alphabet file that should be in resources.
     */
    protected String DEFAULT_ALPHABET;

    /** Stores the constructor parameter. */
    protected AlphabetSerializer<I> alphabetSerializer;

    /** Stores already built alphabets so as not to rebuild them if needed. */
    protected Map<LearnerConfig, Alphabet<I>> alphabetMap = new LinkedHashMap<>();

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alphabetSerializer  the AlphabetSerializer to be used
     */
    public AlphabetBuilderStandard(AlphabetSerializer<I> alphabetSerializer) {
        this.alphabetSerializer = alphabetSerializer;
        this.DEFAULT_ALPHABET = DEFAULT_ALPHABET_NO_EXTENSION + alphabetSerializer.getAlphabetFileExtension();
    }

    @Override
    public Alphabet<I> build(LearnerConfig learnerConfig) {
        if (alphabetMap.containsKey(learnerConfig)) {
            return alphabetMap.get(learnerConfig);
        }

        Alphabet<I> alphabet = null;
        String kind = "provided";
        if (learnerConfig == null || learnerConfig.getAlphabetFilename() == null) {
            kind = "default";
        }

        try (InputStream inputStream = getAlphabetFileInputStream(learnerConfig)) {
            alphabet = alphabetSerializer.read(inputStream);
        } catch (AlphabetSerializerException e) {
            LOGGER.fatal("Failed to instantiate " + kind + " alphabet");
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.debug("Failed to close input stream of " + kind + " alphabet");
        }

        alphabetMap.put(learnerConfig, alphabet);
        return alphabet;
    }

    @Override
    public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
        if (learnerConfig == null || learnerConfig.getAlphabetFilename() == null) {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_ALPHABET);

            if (stream == null) {
                String msg = "Failed to find the default alphabet file in resources: " + DEFAULT_ALPHABET;
                LOGGER.fatal(msg);
                throw new RuntimeException(msg);
            }

            return stream;
        }

        try {
            return new FileInputStream(learnerConfig.getAlphabetFilename());
        } catch (FileNotFoundException e) {
            LOGGER.fatal("Failed to find the provided alphabet file: {}", learnerConfig.getAlphabetFilename());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAlphabetFileExtension() {
        return alphabetSerializer.getAlphabetFileExtension();
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet)
        throws IOException, AlphabetSerializerException {

        try (FileOutputStream alphabetStream = new FileOutputStream(outputFileName)) {
            alphabetSerializer.write(alphabetStream, alphabet);
        }
    }
}
