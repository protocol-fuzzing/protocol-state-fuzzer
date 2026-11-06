package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fingerprints the models learned from the Mealy scenarios and identifies
 * each scenario's endpoint with the fingerprint, all through the command line;
 * see {@link MealyCli}.
 * <p>
 * The learned models are collected into {@code <protocol>/<role>/<scenario>/}
 * of the {@value MealyCli#FINGERPRINT_DIR}, and each group of a protocol and
 * role is fingerprinted into {@code <protocol>/<role>-adg.dot} there, where
 * the scenarios' {@value MealyCli#IDENTIFY} args files expect them.
 * <p>
 * The {@code mismatched} directory has the same layout and ADGs, but each
 * scenario's directory holds the learned model of an inequivalent scenario,
 * so that the endpoint conforms to none of the models it is identified with.
 * <p>
 * A group with a scenario whose learning failed is not fingerprinted, and its
 * tests are skipped; see {@link LearnedMealyScenarios}.
 */
@ExtendWith(LearnedMealyScenarios.Resolver.class)
public class MealyIdentificationIT {

    @TempDir
    static Path workDir;

    /** The fingerprint of each group, as returned by the command line. */
    private static final Map<Scenario.Group, FingerprintNode> FINGERPRINTS = new HashMap<>();

    static List<Scenario> scenarios() {
        return Scenario.discover(MealyScenarioRun.SCENARIOS);
    }

    static List<Scenario.Group> groups() {
        return scenarios().stream().map(Scenario::getGroup).distinct().toList();
    }

    @BeforeAll
    public static void fingerprintLearnedGroups(LearnedMealyScenarios learned) throws IOException {
        for (Scenario.Group group: groups()) {
            List<Scenario> scenarios = scenariosOf(group);
            // the fingerprint needs every model of the group
            if (!learned.allLearned(scenarios)) {
                continue;
            }
            Map<String, MealyScenarioRun> runs = new HashMap<>();
            scenarios.forEach(scenario -> runs.put(scenario.getId(), learned.learning(scenario).run()));
            MealyScenarioRun.collectLearnedModels(runs.values(), fingerprintDir());
            collectInequivalentModels(scenarios, runs);

            Path adgFile = adgFile(fingerprintDir(), group);
            ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = new MealyCliBuilder()
                .process(MealyCli.fingerprint(fingerprintDir().resolve(group.path()), adgFile)).get(0);
            Assertions.assertTrue(result.hasFingerprintResult(), () -> "no fingerprint of the " + group + " models");
            Assertions.assertTrue(Files.isRegularFile(adgFile), () -> "no ADG written for " + group);
            FINGERPRINTS.put(group, result.getFingerprintResult());
            Files.copy(adgFile, adgFile(mismatchedDir(), group));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("groups")
    public void fingerprintSeparatesInequivalentModels(Scenario.Group group) {
        assumeFingerprinted(group);
        MealyAssertions.assertFingerprintSeparatesScenarios(scenariosOf(group), FINGERPRINTS.get(group));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void identifiesAndConformsToEquivalentScenarios(Scenario scenario, @TempDir Path runDir) {
        assumeFingerprinted(scenario.getGroup());
        MealyScenarioRun run = new MealyScenarioRun(scenario, runDir);
        MealyCliBuilder builder = new MealyCliBuilder();

        ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = builder
            .process(run.command(MealyCli.IDENTIFY, Map.of(MealyCli.FINGERPRINT_DIR, fingerprintDir().toString())))
            .get(0);

        Assertions.assertEquals(1, builder.getIdentifiedModels().size(),
            "identifier not run, see the log for the cause");
        Assertions.assertEquals(MealyScenarioRun.equivalentScenarios(scenario, scenariosOf(scenario.getGroup())),
            builder.getIdentifiedModels().get(0));

        // -conformance returns the first identified model that the endpoint conforms to
        Assertions.assertTrue(result.hasLearnerResult(), "no identification result");
        LearnerResult<MealyMachineWrapper<MessageInput, MessageOutput>> conformed = result.getLearnerResult();
        Assertions.assertTrue(conformed.isFromIdentification(), "not an identification result");
        Assertions.assertFalse(conformed.isEmpty(),
            "the endpoint conformed to no identified model, see the log for the cause");
        MealyAssertions.assertLearnedScenarioModel(run, conformed);
        Assertions.assertTrue(Files.isRegularFile(conformed.getLearnedModelFile().toPath()),
            "conforming model not written");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void conformanceRejectsInequivalentModels(Scenario scenario, @TempDir Path runDir) {
        assumeFingerprinted(scenario.getGroup());
        MealyScenarioRun run = new MealyScenarioRun(scenario, runDir);
        MealyCliBuilder builder = new MealyCliBuilder();

        ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = builder
            .process(run.command(MealyCli.IDENTIFY, Map.of(MealyCli.FINGERPRINT_DIR, mismatchedDir().toString())))
            .get(0);

        // the ADG is the same, so the identified models are too
        Assertions.assertEquals(1, builder.getIdentifiedModels().size(),
            "identifier not run, see the log for the cause");
        Assertions.assertEquals(MealyScenarioRun.equivalentScenarios(scenario, scenariosOf(scenario.getGroup())),
            builder.getIdentifiedModels().get(0));

        Assertions.assertTrue(result.hasLearnerResult(), "no identification result");
        Assertions.assertTrue(result.getLearnerResult().isFromIdentification(), "not an identification result");
        Assertions.assertTrue(result.getLearnerResult().isEmpty(),
            "the endpoint conformed to the model of an inequivalent scenario");
    }

    /**
     * Collects into the {@code mismatched} directory, for each scenario of a
     * group, the learned model of the first scenario of the group inequivalent
     * to it.
     */
    private static void collectInequivalentModels(List<Scenario> group, Map<String, MealyScenarioRun> runs)
        throws IOException {
        for (Scenario scenario: group) {
            Set<String> equivalent = MealyScenarioRun.equivalentScenarios(scenario, group);
            Scenario inequivalent = group.stream().filter(other -> !equivalent.contains(other.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No scenario inequivalent to " + scenario));
            Path collected = Files.createDirectories(
                mismatchedDir().resolve(scenario.getGroup().path()).resolve(scenario.getName()));
            Path learned = runs.get(inequivalent.getId()).getOutputDir();
            for (String file: List.of(StateFuzzer.LEARNED_MODEL_FILENAME, MealyScenarioRun.ALPHABET_FILE)) {
                Files.copy(learned.resolve(file), collected.resolve(file));
            }
        }
    }

    /** Skips the calling test if a group was not fingerprinted, since a scenario of it failed to learn. */
    private static void assumeFingerprinted(Scenario.Group group) {
        Assumptions.assumeTrue(FINGERPRINTS.containsKey(group),
            () -> "learning a scenario of " + group + " failed, see MealyScenariosIT");
    }

    private static List<Scenario> scenariosOf(Scenario.Group group) {
        return scenarios().stream().filter(scenario -> scenario.getGroup().equals(group)).toList();
    }

    private static Path fingerprintDir() {
        return workDir.resolve("fingerprint");
    }

    private static Path mismatchedDir() {
        return workDir.resolve("mismatched");
    }

    /** Returns where the {@value MealyCli#IDENTIFY} args files expect the ADG of a group. */
    private static Path adgFile(Path dir, Scenario.Group group) {
        return dir.resolve(group.path() + "-adg.dot");
    }
}
