package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.AlphabetOptionProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import net.automatalib.words.Alphabet;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface AlphabetBuilder {
    String DEFAULT_ALPHABET_NO_EXTENSION = "default_alphabet";

    Alphabet<AbstractInput> build(AlphabetOptionProvider config);

    InputStream getAlphabetFileInputStream(AlphabetOptionProvider config);

    String getAlphabetFileExtension();

    void exportAlphabetToFile(String outputFileName, Alphabet<AbstractInput> alphabet) throws FileNotFoundException,
            AlphabetSerializerException;
}
