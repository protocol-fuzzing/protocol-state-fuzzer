package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import de.learnlib.ralib.words.ParameterizedSymbol;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RAAlphabetBuilder implements AlphabetBuilder<ParameterizedSymbol> {

    private ParameterizedSymbol[] symbols;

    public RAAlphabetBuilder(ParameterizedSymbol ...symbols) {
        this.symbols = symbols;
    }

    @Override
    public Alphabet<ParameterizedSymbol> build(LearnerConfig learnerConfig) {
        return new ListAlphabet<>(Arrays.asList(symbols));
    }

    @Override
    public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
        return null;
    }

    @Override
    public String getAlphabetFileExtension() {
        return null;
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<ParameterizedSymbol> alphabet)
            throws IOException, AlphabetSerializerException {
    }

}
