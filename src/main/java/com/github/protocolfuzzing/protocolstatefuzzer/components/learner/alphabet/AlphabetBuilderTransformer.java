package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * The transformer implementation of the AlphabetBuilder that reads an
 * {@code Alphabet<RI>} and then transforms it to an {@code Alphabet<TI>}.
 * <p>
 * An {@code AlphabetBuilderStandard<RI>} is used for the {@code Alphabet<RI>}
 *
 * @param <RI>  the type of read inputs
 * @param <TI>  the type of transformed inputs
 */
public abstract class AlphabetBuilderTransformer<RI, TI> implements AlphabetBuilder<TI> {

    /** Stores the provided standard builder used for the {@code Alphabet<RI>} */
    protected AlphabetBuilderStandard<RI> alphabetBuilderStandard;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alphabetBuilderStandard  the AlphabetBuilderStandard to be used for the read inputs
     */
    public AlphabetBuilderTransformer(AlphabetBuilderStandard<RI> alphabetBuilderStandard) {
        this.alphabetBuilderStandard = alphabetBuilderStandard;
    }

    @Override
    public Alphabet<TI> build(LearnerConfig learnerConfig) {
        Alphabet<RI> rAlphabet = alphabetBuilderStandard.build(learnerConfig);
        Alphabet<TI> tAlphabet = new ListAlphabet<>(
            rAlphabet.stream().map(ri -> toTransformedInput(ri)).collect(Collectors.toList())
        );
        return tAlphabet;
    }

    @Override
    public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
        return alphabetBuilderStandard.getAlphabetFileInputStream(learnerConfig);
    }

    @Override
    public String getAlphabetFileExtension() {
        return alphabetBuilderStandard.getAlphabetFileExtension();
    }

    @Override
    public void exportAlphabetToFile(String outputFileName, Alphabet<TI> tAlphabet)
        throws IOException, AlphabetSerializerException {
        Alphabet<RI> rAlphabet = new ListAlphabet<>(
            tAlphabet.stream().map(ti -> fromTransformedInput(ti)).collect(Collectors.toList())
        );
        alphabetBuilderStandard.exportAlphabetToFile(outputFileName, rAlphabet);
    }

    /**
     * Converts the read input to transformed input.
     *
     * @param ri  the read input to be converted
     * @return    the converted transformed input
     */
    public abstract TI toTransformedInput(RI ri);

    /**
     * Converts the transformed input to read input.
     *
     * @param ti  the transformed input to be converted
     * @return    the converted read input
     */
    public abstract RI fromTransformedInput(TI ti);
}
