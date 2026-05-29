package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.BiPredicate;

public class DiffTesterTest {

    private String resourcePath(String filename) {
        return getClass().getClassLoader().getResource("difftest/" + filename).getPath();
    }

    private DiffTesterConfig buildConfig(String modelAPath, String modelBPath, Alphabet<String> alphabet) {
        return new DiffTesterConfigStandard() {
            {
                modelA = modelAPath;
                modelB = modelBPath;
            }
        };
    }

    private AlphabetBuilderTransformer<String, String> alphabetTransformer(Alphabet<String> alphabet) {
        return new AlphabetBuilderTransformer<String, String>(null) {
            @Override
            public Alphabet<String> build(LearnerConfig learnerConfig) {
                return alphabet;
            }

            @Override
            public String toTransformedInput(String s) {
                return s;
            }

            @Override
            public String fromTransformedInput(String s) {
                return s;
            }
        };
    }

    private static final Alphabet<String> CLIENT_HELLO_FINISHED_ALPHABET = Alphabets.fromArray("CLIENT_HELLO",
        "FINISHED");

    @Test
    public void identicalModels_noDivergence() {
        DiffTesterConfig config = buildConfig(
            resourcePath("simple_2state_base.dot"),
            resourcePath("simple_2state_base.dot"),
            CLIENT_HELLO_FINISHED_ALPHABET);

        DiffTestResult result = new DiffTesterStandard<>(config, alphabetTransformer(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.areModelsEquivalent());
    }

    @Test
    public void divergingModels_divergencesFound() {
        DiffTesterConfig config = buildConfig(
            resourcePath("simple_2state_base.dot"),
            resourcePath("simple_2state_divergence_depth0.dot"),
            CLIENT_HELLO_FINISHED_ALPHABET);

        DiffTestResult result = new DiffTesterStandard<>(config, alphabetTransformer(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.areModelsEquivalent());
        Assert.assertFalse(result.getDivergences().isEmpty());
    }

    @Test
    public void invalidModelPath_returnsEmptyResult() {
        DiffTesterConfig config = buildConfig(
            "nonexistent.dot",
            resourcePath("simple_2state_base.dot"),
            CLIENT_HELLO_FINISHED_ALPHABET);

        DiffTestResult result = new DiffTesterStandard<>(config, alphabetTransformer(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void customOutputEquivalence_allOutputsEqual() {
        DiffTesterConfig config = new DiffTesterConfigStandard() {
            {
                modelA = resourcePath("simple_2state_base.dot");
                modelB = resourcePath("simple_2state_divergence_depth0.dot");
            }

            @Override
            public BiPredicate<String, String> getOutputEquivalence() {
                return (a, b) -> true;
            }
        };

        DiffTestResult result = new DiffTesterStandard<>(config, alphabetTransformer(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.areModelsEquivalent());
    }
}
