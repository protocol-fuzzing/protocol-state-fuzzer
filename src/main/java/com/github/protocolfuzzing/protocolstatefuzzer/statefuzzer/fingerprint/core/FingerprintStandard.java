package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm.FingerprintExtractDecisionTree;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintAdgWriter;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintGenerateLTS;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Standard implementation of the fingerprint extraction process
 *
 * @param <I> the type of the input symbols in the Mealy machines
 */
public class FingerprintStandard<I> implements FingerprintExtraction {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter */
    protected FingerprintEnabler fingerprintEnabler;

    /** The alphabet builder */
    protected AlphabetBuilder<I> alphabetBuilder;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param fingerprintEnabler the configuration that enables the fingerprinting
     * @param alphabetBuilder    the alphabet builder used to build the {@code Alphabet<String>}
     */
    public FingerprintStandard(FingerprintEnabler fingerprintEnabler,
        AlphabetBuilder<I> alphabetBuilder) {
        this.fingerprintEnabler = fingerprintEnabler;
        this.alphabetBuilder = alphabetBuilder;
    }

    /**
     * Runs the fingerprinting algorithm and returns a decision tree (Adaptive Distinguish Graph - ADG)
     * <p>
     * Models are loaded from the given directory provided in the {@link FingerprintConfig}
     * and the result is saved in the output file (or the "adg.dot" file if no output file is provided).
     *
     * @return the root of the ADG as a {@link FingerprintNode}
     */
    @Override
    public FingerprintNode run() throws RuntimeException {
        FingerprintParser<I> parser = new FingerprintParser<>(alphabetBuilder);
        List<String> modelNames = new ArrayList<>();
        List<MealyMachineWrapper<String, String>> machines;
        try {
            machines = parser.loadDirectory(fingerprintEnabler.getFingerprintConfig().getModelsPath(), modelNames);
        }
        catch (Exception e) {
            LOGGER.error("Error while getting models");
            return null;
        }

        List<Set<String>> modelSets = new ArrayList<>();

        // TODO: Make it so that there is deduplication of model

        for (String name: modelNames) {
            Set<String> set = new HashSet<>();
            set.add(name);
            modelSets.add(set);
        }

        FingerprintGenerateLTS converter = new FingerprintGenerateLTS();
        FingerprintAutomaton A = new FingerprintAutomaton(converter);

        A.calculateCombined(machines, modelNames);

        LOGGER.info("Combined LTS: %d states%n", A.getCombined().automaton.getNumStates());
        LOGGER.info("Alphabet: %d inputs, %d outputs (+δ)%n",
            converter.numInputs(), converter.numOutputs() - 1);

        LOGGER.info("Start the fingerprint extraction process");
        A.expandCombinedWithNames(modelSets);

        FingerprintNode adg = null;
        try {
            adg = new FingerprintExtractDecisionTree(A).compute();
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            throw new RuntimeException(e);
        }

        try {
            LOGGER.info("Writing adg to output");
            if (fingerprintEnabler.getFingerprintConfig().getOutputPath() != null) {
                FingerprintAdgWriter.write(adg, Paths.get(fingerprintEnabler.getFingerprintConfig().getOutputPath()));
            } else {
                FingerprintAdgWriter.write(adg, Paths.get("adg.dot"));
            }
        }
        catch (IOException e) {
            LOGGER.error("Error while writing adg, {}", e.getMessage());
        }

        return adg;

    }
}
