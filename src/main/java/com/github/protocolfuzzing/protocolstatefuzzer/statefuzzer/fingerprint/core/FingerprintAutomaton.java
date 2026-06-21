package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm.FingerprintComputeCompatibility;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.algorithm.FingerprintSplittingGraphExtraction;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io.FingerprintGenerateLTS;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Wraps the combined LTS, compatibility relation, and splitting graph for fingerprinting purposes */
public class FingerprintAutomaton {
    private FingerprintGenerateLTS.CombinedLTS combined;
    private FingerprintSplittingGraph splittingGraph;
    private FingerprintSplittingGraph.CompatibilityRelation compat;
    private FingerprintGenerateLTS converter;

    /**
     * Constructor for the FingerprintAutomaton, which represents the combined LTS of the models,
     * the compatibility relation of state pairs, and the splitting graph.
     *
     * @param converter the converter to use to generate the combined LTS of the models
     */
    public FingerprintAutomaton(FingerprintGenerateLTS converter) {
        this.converter = converter;
        this.compat = null;
        this.splittingGraph = null;
        this.combined = null;
    }

    /**
     * Calculates the combined LTS of the models, the compatibility relation of state pairs, and the splitting graph.
     *
     * @param machines   the list of models to combine
     * @param modelNames the list of model names, in the same order as the machines list
     */
    public void calculateCombined(List<MealyMachineWrapper<String, String>> machines, List<String> modelNames) {
        try {
            this.combined = converter.combine(machines, modelNames);
            this.compat = FingerprintComputeCompatibility.computeCompatibility(this);
            this.splittingGraph = new FingerprintSplittingGraphExtraction(this).build();
        }
        catch (IllegalArgumentException e) {
            throw e;
        }

    };

    /**
     * Getter for the combined LTS of the models, which is represented as a {@link FingerprintGenerateLTS.CombinedLTS}
     * object.
     *
     * @return the combined LTS of the models
     */
    public FingerprintGenerateLTS.CombinedLTS getCombined() {
        return combined;
    }

    /**
     * Getter for the splitting graph, which is represented as a {@link FingerprintSplittingGraph} object.
     *
     * @return the splitting graph of the combined LTS of the models
     */
    public FingerprintSplittingGraph getSplittingGraph() {
        return splittingGraph;
    }

    /**
     * Getter for the compatibility relation of state pairs, which is represented as a
     * {@link FingerprintSplittingGraph.CompatibilityRelation} object.
     *
     * @return the compatibility relation of state pairs in the combined LTS of the models
     */
    public FingerprintSplittingGraph.CompatibilityRelation getCompat() {
        return compat;
    }

    /**
     * Getter for the converter used to generate the combined LTS of the models, which is represented as a
     * {@link FingerprintGenerateLTS} object.
     *
     * @return the converter used to generate the combined LTS of the models
     */
    public FingerprintGenerateLTS getConverter() {
        return converter;
    }

    /**
     * Expands the combined LTS of the models by replacing the representative model with all the models that had
     * equivalent Mealy Machines
     *
     * @param  combined the initial combined LTS of the models
     * @param  implSets the list of sets of model names, where each set corresponds to a set of equivalent models
     *
     * @return          the expanded combined LTS of the models with the full models names
     */
    public static FingerprintGenerateLTS.CombinedLTS expandImplNames(
        FingerprintGenerateLTS.CombinedLTS combined, List<Set<String>> implSets) {
        List<String> expandedNames = new ArrayList<>();
        for (Set<String> s: implSets)
            expandedNames.add(String.join(",", s));
        return new FingerprintGenerateLTS.CombinedLTS(
            combined.automaton, combined.stateToModel, combined.modelInitials,
            expandedNames, combined.offsets, combined.origCounts,
            combined.midCounts, combined.stateIndexMaps) {
            @Override
            public Set<String> modelsIn(Set<Integer> states) {
                Set<String> result = new LinkedHashSet<>();
                for (int s: states) {
                    int m = stateToModel[s];
                    if (m >= 0)
                        result.addAll(implSets.get(m));
                }
                return result;
            }
        };
    }

    /**
     * Expands the combined LTS of the models by replacing the representative model with all the models that had
     * equivalent Mealy Machines
     *
     * @param implSets the list of sets of model names, where each set corresponds to a set of equivalent models
     */
    public void expandCombinedWithNames(List<Set<String>> implSets) {
        this.combined = expandImplNames(this.combined, implSets);
    }
}
