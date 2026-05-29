package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Performs differential testing of two Mealy machine models loaded from DOT files.
 * <p>
 * It uses a {@link DiffTesterConfig} to obtain the model paths and the alphabet,
 * which are used to load the models and run the {@link DifferentialOracle}.
 */
public class DiffTesterStandard<I> implements DiffTester {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter */
    private final DiffTesterEnabler diffTesterEnabler;

    /** Stores the constructor parameter */
    private final AlphabetBuilderTransformer<I, String> alphabetBuilderTransformer;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param config the configuration for the differential testing
     */
    public DiffTesterStandard(DiffTesterEnabler diffTesterEnabler,
        AlphabetBuilderTransformer<I, String> alphabetBuilderTransformer) {
        this.diffTesterEnabler = diffTesterEnabler;
        this.alphabetBuilderTransformer = alphabetBuilderTransformer;
    }

    /**
     * Runs the differential testing and returns the result.
     * <p>
     * It loads the two models from the paths provided in the {@link DiffTesterConfig},
     * runs the {@link DifferentialOracle} on them and returns the corresponding {@link DiffTestResult}.
     *
     * @return the DiffTestResult containing the divergences found between the two models
     */
    public DiffTestResult run() {
        try {
            DiffTesterConfig diffTesterConfig = diffTesterEnabler.getDiffTesterConfig();
            LearnerConfig learnerConfig = diffTesterEnabler.getLearnerConfig();
            Alphabet<String> alphabet = alphabetBuilderTransformer.build(learnerConfig);

            MealyIOProcessor<String, String> processor = new MealyIOProcessor<>(alphabet, stringOutputBuilder);

            MealyMachine<?, String, ?, String> modelA = loadModel(diffTesterConfig.getModelA(), processor);
            MealyMachine<?, String, ?, String> modelB = loadModel(diffTesterConfig.getModelB(), processor);

            BiPredicate<String, String> equivalence = diffTesterConfig.getOutputEquivalence();
            DifferentialOracle<String, String> oracle = equivalence != null
                ? new DifferentialOracle<>(equivalence)
                : new DifferentialOracle<>();

            List<DivergenceRecord<String, String>> divergences = oracle.analyse(modelA, modelB, alphabet);

            LOGGER.info("Differential testing completed");

            return new DiffTestResult(divergences, extractModelName(diffTesterConfig.getModelA()),
                extractModelName(diffTesterConfig.getModelB()));
        }
        catch (IOException | FormatException e) {
            LOGGER.error("Failed to load models for differential testing {}", e.getMessage());
            return new DiffTestResult();
        }
    }

    /**
     * Builds an OutputBuilder that returns the output name as-is.
     * <p>
     * Used to map the output labels from the DOT file directly to their string representation
     * without any transformation.
     */
    private final OutputBuilder<String> stringOutputBuilder = new OutputBuilder<String>() {
        @Override
        public String buildOutputExact(String name) {
            return name;
        }
    };

    /**
     * Loads a Mealy machine model from the given DOT file path.
     *
     * @param  <S>             the state type of the loaded model
     * @param  path            the path to the DOT file
     * @param  processor       the processor for inputs and outputs
     *
     * @return                 the loaded Mealy machine model
     *
     * @throws IOException     if an error occurs while reading the DOT file
     * @throws FormatException if the DOT file has an invalid format
     */
    private <S> MealyMachine<S, String, ?, String> loadModel(String path, MealyIOProcessor<String, String> processor)
        throws IOException, FormatException {
        @SuppressWarnings("unchecked")
        MealyMachine<S, String, ?, String> model = (MealyMachine<S, String, ?, String>) ModelFactory
            .buildProtocolModel(path, processor);
        return model;
    }

    /**
     * Extracts the model name from the given file path by removing the
     * directory and .dot extension.
     * <p>
     * Example: {@code "output/gnutls_psk.dot"} returns {@code "gnutls_psk"}.
     *
     * @param  path the path to the model DOT file
     *
     * @return      the model name
     */
    private String extractModelName(String path) {
        if (path == null)
            return null;
        java.nio.file.Path fileName = Paths.get(path).getFileName();
        if (fileName == null)
            return null;
        return fileName.toString().replace(".dot", "");
    }
}
