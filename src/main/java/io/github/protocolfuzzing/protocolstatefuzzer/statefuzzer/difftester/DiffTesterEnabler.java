package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;

/**
 * Interface that enables testing with the DiffTester by extending the necessary interfaces.
 */
public interface DiffTesterEnabler {

    /**
     * Returns the LearnerConfig.
     *
     * @return the LearnerConfig
     */
    LearnerConfig getLearnerConfig();

    /**
     * Returns the DiffTesterConfig.
     *
     * @return the DiffTesterConfig
     */
    DiffTesterConfig getDiffTesterConfig();
}
