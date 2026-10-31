package io.github.protocolfuzzing.protocolstatefuzzer.testutil;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AlphabetBuilder} that always returns the same in-memory alphabet,
 * has no backing file and does not export.
 *
 * @param <I> the type of the input symbols
 */
public class FixedAlphabetBuilder<I> implements AlphabetBuilder<I> {

    protected Alphabet<I> alphabet;

    public FixedAlphabetBuilder(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
    }

    @SafeVarargs
    public static <I> FixedAlphabetBuilder<I> of(I... symbols) {
        List<I> symbolList = new ArrayList<>(symbols.length);
        for (I symbol: symbols) {
            symbolList.add(symbol);
        }
        return new FixedAlphabetBuilder<>(new ListAlphabet<>(symbolList));
    }

    @Override
    public Alphabet<I> build(LearnerConfig learnerConfig) {
        return alphabet;
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
    public void exportAlphabetToFile(String outputFileName, Alphabet<I> alphabet) {}
}
