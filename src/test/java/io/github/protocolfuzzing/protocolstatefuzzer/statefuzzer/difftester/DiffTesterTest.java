package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.FixedAlphabetBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

public class DiffTesterTest {

    private String resourcePath(String filename) {
        return getClass().getClassLoader().getResource("difftest/" + filename).getPath();
    }

    private DiffTesterConfig buildConfig(String modelAPath, String modelBPath) {
        return new DiffTesterConfigStandard() {
            {
                modelA = modelAPath;
                modelB = modelBPath;
            }
        };
    }

    private static final Alphabet<String> CLIENT_HELLO_FINISHED_ALPHABET = Alphabets.fromArray("CLIENT_HELLO",
        "FINISHED");

    @Test
    public void identicalModels_noDivergence() {
        DiffTesterConfig config = buildConfig(
            resourcePath("simple_2state_base.dot"),
            resourcePath("simple_2state_base.dot"));

        DiffTestResult result = new DiffTesterStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.areModelsEquivalent());
    }

    @Test
    public void divergingModels_divergencesFound() {
        DiffTesterConfig config = buildConfig(
            resourcePath("simple_2state_base.dot"),
            resourcePath("simple_2state_divergence_depth0.dot"));

        DiffTestResult result = new DiffTesterStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.areModelsEquivalent());
        Assertions.assertFalse(result.getDivergences().isEmpty());
    }

    @Test
    public void invalidModelPath_returnsEmptyResult() {
        DiffTesterConfig config = buildConfig(
            "nonexistent.dot",
            resourcePath("simple_2state_base.dot"));

        DiffTestResult result = new DiffTesterStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertTrue(result.isEmpty());
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

        DiffTestResult result = new DiffTesterStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.areModelsEquivalent());
    }
}
