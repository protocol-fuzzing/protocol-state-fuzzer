package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class FingerprintTest {

    private String resourcePath(String filename) {
        return getClass().getClassLoader().getResource("fingerprint/" + filename).getPath();
    }

    private FingerprintConfig buildConfig(String modelsPath, String outputPath) {
        return new FingerprintConfigStandard() {
            {
                fingerprintPath = modelsPath;
                outputFilename = outputPath;
            }
        };
    }

    private AlphabetBuilder<String> alphabetBuilder(Alphabet<String> alphabet) {
        return new AlphabetBuilder<String>() {
            @Override
            public Alphabet<String> build(LearnerConfig learnerConfig) {
                return alphabet;
            }

            @Override
            public InputStream getAlphabetFileInputStream(LearnerConfig learnerConfig) {
                return null;
            }

            @Override
            public String getAlphabetFileExtension() {
                return null;
            }

            @Override
            public void exportAlphabetToFile(String outputFileName, Alphabet<String> alphabet) {}
        };
    }

    private static final Alphabet<String> CLIENT_HELLO_FINISHED_ALPHABET = Alphabets.fromArray("CLIENT_HELLO",
        "FINISHED");
    private static final Alphabet<String> CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET = Alphabets
        .fromArray("CLIENT_HELLO", "FINISHED", "APPLICATION");

    private static final String outputFolder = "src/test/resources/fingerprint/output/";

    private static boolean checkAllLeavesSingleModel(FingerprintNode node) {
        boolean holds = true;
        if (node.isLeaf())
            return node.getModels().size() == 1;
        for (FingerprintNode c: node.getChildren().values()) {
            holds &= checkAllLeavesSingleModel(c);
        }
        return holds;
    }

    @Test
    public void identicalModels_allCompatible() {
        FingerprintConfig config = buildConfig(
            resourcePath("identical"),
            outputFolder + "adg_identical.dot");

        FingerprintNode result = new FingerprintStandard<>(config, alphabetBuilder(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertTrue(result.getEdgeLabel() == null);
        Assert.assertTrue(result.getChildren().isEmpty());
        Assert.assertTrue(result.getModels().size() == 3); // The folder identical should contain 3 folders of identical
                                                           // models
    }

    @Test
    public void differentModels_sameAlphabet_fullFIngerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("same_alphabet"),
            outputFolder + "adg_same_alphabet.dot");

        FingerprintNode result = new FingerprintStandard<>(config, alphabetBuilder(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertTrue(result.getEdgeLabel() == null);
        Assert.assertTrue(checkAllLeavesSingleModel(result));

    }

    @Test
    public void differentModels_diffAlphabet_fullFingerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("diff_alphabet"),
            outputFolder + "adg_diff_alphabet.dot");

        FingerprintNode result = new FingerprintStandard<>(config,
            alphabetBuilder(CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET))
            .run();

        Assert.assertTrue(result.getEdgeLabel() == null);
        Assert.assertTrue(checkAllLeavesSingleModel(result));

    }

    @Test
    public void differentModels_diffAlphabet_partialFingerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("diff_alphabet_partial"),
            outputFolder + "adg_diff_alphabet_partial.dot");

        FingerprintNode result = new FingerprintStandard<>(config,
            alphabetBuilder(CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET))
            .run();

        Assert.assertTrue(result.getEdgeLabel() == null);
        Assert.assertFalse(checkAllLeavesSingleModel(result));

    }

    @Test
    public void invalidModelPath_returnsEmptyResult() {
        FingerprintConfig config = buildConfig(
            "nonexistent",
            "adg_non.dot");

        FingerprintNode result = new FingerprintStandard<>(config, alphabetBuilder(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assert.assertTrue(result == null);
    }

}
