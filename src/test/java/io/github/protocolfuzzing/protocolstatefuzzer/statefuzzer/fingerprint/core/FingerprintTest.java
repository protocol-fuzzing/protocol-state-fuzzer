package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.FixedAlphabetBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

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

    private static final Alphabet<String> CLIENT_HELLO_FINISHED_ALPHABET = Alphabets.fromArray("CLIENT_HELLO",
        "FINISHED");
    private static final Alphabet<String> CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET = Alphabets
        .fromArray("CLIENT_HELLO", "FINISHED", "APPLICATION");

    @TempDir
    Path outputDir;

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
            outputDir.resolve("adg_identical.dot").toString());

        FingerprintNode result = new FingerprintStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertTrue(result.getEdgeLabel() == null);
        Assertions.assertTrue(result.getChildren().isEmpty());
        Assertions.assertTrue(result.getModels().size() == 3); // The directory identical should contain 3 directories
                                                               // of
        // identical models
    }

    @Test
    public void differentModels_sameAlphabet_fullFIngerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("same_alphabet"),
            outputDir.resolve("adg_same_alphabet.dot").toString());

        FingerprintNode result = new FingerprintStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertTrue(result.getEdgeLabel() == null);
        Assertions.assertTrue(checkAllLeavesSingleModel(result));

    }

    @Test
    public void differentModels_diffAlphabet_fullFingerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("diff_alphabet"),
            outputDir.resolve("adg_diff_alphabet.dot").toString());

        FingerprintNode result = new FingerprintStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET))
            .run();

        Assertions.assertTrue(result.getEdgeLabel() == null);
        Assertions.assertTrue(checkAllLeavesSingleModel(result));

    }

    @Test
    public void differentModels_diffAlphabet_partialFingerprintFound() {
        FingerprintConfig config = buildConfig(
            resourcePath("diff_alphabet_partial"),
            outputDir.resolve("adg_diff_alphabet_partial.dot").toString());

        FingerprintNode result = new FingerprintStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_APPLICATION_ALPHABET))
            .run();

        Assertions.assertTrue(result.getEdgeLabel() == null);
        Assertions.assertFalse(checkAllLeavesSingleModel(result));

    }

    @Test
    public void invalidModelPath_returnsEmptyResult() {
        FingerprintConfig config = buildConfig(
            "nonexistent",
            outputDir.resolve("adg_non.dot").toString());

        FingerprintNode result = new FingerprintStandard<>(config,
            new FixedAlphabetBuilder<>(CLIENT_HELLO_FINISHED_ALPHABET))
            .run();

        Assertions.assertTrue(result == null);
    }

}
