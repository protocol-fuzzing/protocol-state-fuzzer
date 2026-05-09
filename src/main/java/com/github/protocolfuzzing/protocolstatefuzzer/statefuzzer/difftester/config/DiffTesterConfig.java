package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DifferentialOracle;
import net.automatalib.alphabet.Alphabet;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Interface for the configuration for differential testing.
 */
public interface DiffTesterConfig {

    /**
     * Returns the path to the first model file (modelA).
     *
     * @return the path to modelA
     */
    String getModelA();

    /**
     * Returns the path to the first model file (modelB).
     *
     * @return the path to modelB
     */
    String getModelB();

    /**
     * Returns the alphabet to be used during differential testing
     *
     * @return the alphabet to be used during differential testing
     */
    Alphabet<String> getAlphabet();

    /**
     * Retrusn a custom output equivalence prdeicate to be used during differentail testing.
     * <p>
     * Default value: null, which causes the {@link DifferentialOracle} to use
     * strict output equality via {@link Objects#equals}.
     *
     * @return the custom output equivalence predicate, or null for strict equality
     */
    default BiPredicate<String, String> getOutputEquivalence() {
        return null;
    }

    /**
     * Returns the singleton PropertyResolver instance.
     * <p>
     * Default: the singleton instance.
     *
     * @return the singleton PropertyResolver instance
     */
    default PropertyResolver getPropertyResolver() {
        return PropertyResolver.getInstance();
    }
}
