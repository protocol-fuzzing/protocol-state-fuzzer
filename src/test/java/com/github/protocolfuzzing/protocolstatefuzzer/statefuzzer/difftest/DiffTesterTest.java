package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
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

            @Override
            public Alphabet<String> getAlphabet() {
                return alphabet;
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

        DiffTestResult result = new DiffTester(config).run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isEquivalent());
    }

    @Test
    public void divergingModels_divergencesFound() {
        DiffTesterConfig config = buildConfig(
            resourcePath("simple_2state_base.dot"),
            resourcePath("simple_2state_divergence_depth0.dot"),
            CLIENT_HELLO_FINISHED_ALPHABET);

        DiffTestResult result = new DiffTester(config).run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isEquivalent());
        Assert.assertFalse(result.getDivergences().isEmpty());
    }

    @Test
    public void invalidModelPath_returnsEmptyResult() {
        DiffTesterConfig config = buildConfig(
            "nonexistent.dot",
            resourcePath("simple_2state_base.dot"),
            CLIENT_HELLO_FINISHED_ALPHABET);

        DiffTestResult result = new DiffTester(config).run();

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
            public Alphabet<String> getAlphabet() {
                return CLIENT_HELLO_FINISHED_ALPHABET;
            }

            @Override
            public BiPredicate<String, String> getOutputEquivalence() {
                return (a, b) -> true;
            }
        };

        DiffTestResult result = new DiffTester(config).run();

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isEquivalent());
    }
}
