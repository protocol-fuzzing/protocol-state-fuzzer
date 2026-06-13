package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.PropertyResolver;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DifferentialOracle;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Interface for the configuration for differential testing.
 */
public interface DiffTesterConfig extends DiffTesterEnabler {

    /**
     * Returns the path to the first model file (modelA).
     *
     * @return the path to modelA
     */
    String getModelA();

    /**
     * Returns the path to the second model file (modelB).
     *
     * @return the path to modelB
     */
    String getModelB();

    /**
     * Returns the custom name for modelA.
     *
     * @return the custom name for modelA
     */
    String getModelAName();

    /**
     * Returns the custom name for modelB.
     *
     * @return the custom name for modelB
     */
    String getModelBName();

    /**
     * Returns a custom output equivalence predicate to be used during differential testing.
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

    /**
     * Returns the LearnerConfig.
     * <p>
     * Default value: a new empty LearnerConfig.
     *
     * @return the LearnerConfig
     */
    @Override
    default LearnerConfig getLearnerConfig() {
        return new LearnerConfig() {};
    }

    /**
     * Returns the DiffTesterConfig.
     * <p>
     * Default value: the instance itself.
     *
     * @return the DiffTesterConfig
     */
    @Override
    default DiffTesterConfig getDiffTesterConfig() {
        return this;
    }
}
