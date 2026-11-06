package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs each Mealy scenario under {@code src/test/resources/e2e/mealy} through
 * the command line, with the args files in its {@code args/} directory; see
 * {@link ScenarioCli}.
 */
// first, so that a learning failure is reported before the tests skipping on it
@Order(1)
@ExtendWith(LearnedMealyScenarios.Resolver.class)
public class MealyScenariosIT {

    static List<Scenario> scenarios() {
        return Scenario.discover(MealyScenarioRun.SCENARIOS);
    }

    static List<Scenario> learnScenarios() {
        return scenariosWith(ScenarioCli.LEARN);
    }

    static List<Scenario> testScenarios() {
        return scenariosWith(ScenarioCli.TEST);
    }

    static List<Scenario> probeScenarios() {
        return scenariosWith(MealyCli.PROBE);
    }

    private static List<Scenario> scenariosWith(String argsFile) {
        return scenarios().stream().filter(scenario -> scenario.findFile(argsFile).isPresent()).toList();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void hasArgsFiles(Scenario scenario) {
        for (String argsFile: List.of(ScenarioCli.LEARN, ScenarioCli.TEST, MealyCli.IDENTIFY)) {
            Assertions.assertTrue(scenario.findFile(argsFile).isPresent(), () -> "no " + argsFile);
        }
        Assertions.assertEquals(scenario.getLatency() > 0, scenario.findFile(MealyCli.PROBE).isPresent(),
            () -> MealyCli.PROBE + " is expected exactly for scenarios with latency");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void argsFilesSelectOwnScenario(Scenario scenario) {
        ScenarioCli.assertArgsFilesSelectScenario(scenario,
            List.of(ScenarioCli.LEARN, ScenarioCli.TEST, MealyCli.PROBE, MealyCli.IDENTIFY));

        // test checks against the scenario's own model
        Assertions.assertEquals(
            "${" + ScenarioCli.SCENARIOS_DIR + "}/" + scenario.getId() + "/" + MealyModelEndpoint.MODEL_FILE,
            ScenarioCli.optionValue(scenario.requireFile(ScenarioCli.TEST), "-testSpecification"),
            () -> "-testSpecification of " + ScenarioCli.TEST);

        // identify uses the fingerprint of its own protocol and role
        String groupDir = "${" + MealyCli.FINGERPRINT_DIR + "}/" + scenario.getGroup().path();
        Path identify = scenario.requireFile(MealyCli.IDENTIFY);
        Assertions.assertEquals(groupDir + "-adg.dot", ScenarioCli.optionValue(identify, "-identify"),
            () -> "-identify of " + MealyCli.IDENTIFY);
        Assertions.assertEquals(groupDir + "/", ScenarioCli.optionValue(identify, "-conformance"),
            () -> "-conformance of " + MealyCli.IDENTIFY);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("learnScenarios")
    public void learnsScenarioModel(Scenario scenario, LearnedMealyScenarios learned) {
        // learning, then comparing the learned model to the expected one, in one call
        LearnedMealyScenarios.Learning learning = learned.learning(scenario);
        if (learning.failure() != null) {
            Assertions.fail("the command line threw", learning.failure());
        }
        MealyScenarioRun run = learning.run();
        Path outputDir = run.getOutputDir();
        List<ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>>> results = learning.results();

        Assertions.assertEquals(2, results.size(), "one result per command");
        Assertions.assertTrue(results.get(0).hasLearnerResult(), "no learner result, see the log for the cause");
        MealyAssertions.assertLearnedScenarioModel(run, results.get(0).getLearnerResult());
        Assertions.assertTrue(results.get(1).hasDiffTestResult(), "no differential testing result");
        MealyAssertions.assertNoDivergences(results.get(1).getDiffTestResult());

        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.STATISTICS_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.MAPPER_CONNECTION_CONFIG_FILENAME)),
            "default mapper connection config not copied");

        ScenarioCli.assertArgsKept(learning.learn(), outputDir);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testScenarios")
    public void testRunnerMatchesModel(Scenario scenario, @TempDir Path workDir) {
        MealyScenarioRun run = new MealyScenarioRun(scenario, workDir);
        run.writeTests();
        MealyCliBuilder builder = new MealyCliBuilder();

        ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = builder
            .process(run.command(ScenarioCli.TEST)).get(0);

        Assertions.assertTrue(result.hasLearnerResult() && result.getLearnerResult().isFromTest(),
            "no test result, see the log for the cause");
        Assertions.assertEquals(1, builder.getTestRuns().size(), "test runner not run, see the log for the cause");
        MealyCliBuilder.TestRun testRun = builder.getTestRuns().get(0);
        MealyAssertions.assertTestResultsMatchModel(run, testRun.results(), testRun.specification());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("probeScenarios")
    public void timingProbeFindsLatency(Scenario scenario, @TempDir Path workDir) {
        MealyScenarioRun run = new MealyScenarioRun(scenario, workDir);
        run.writeTests();
        MealyCliBuilder builder = new MealyCliBuilder();

        builder.process(run.command(MealyCli.PROBE));

        Assertions.assertEquals(1, builder.getProbedTimes().size(), "timing probe not run, see the log for the cause");
        MealyAssertions.assertProbedLatency(scenario, builder.getProbedTimes().get(0));
    }
}
