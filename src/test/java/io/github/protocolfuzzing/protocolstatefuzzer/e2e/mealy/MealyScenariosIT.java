package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Runs each Mealy scenario under {@code src/test/resources/e2e/mealy} through
 * the command line, with the args files in its {@code args/} directory.
 * See {@link ScenarioCli}.
 */
@Order(1)
@ExtendWith(LearnedMealyScenarios.Resolver.class)
public class MealyScenariosIT {

    static List<Scenario> scenarios() throws IOException {
        return Scenario.discover(MealyScenarioRun.SCENARIOS);
    }

    static List<Scenario> learnScenarios() throws IOException {
        return scenariosWith(ScenarioCli.LEARN);
    }

    static List<Scenario> testScenarios() throws IOException {
        return scenariosWith(ScenarioCli.TEST);
    }

    static List<Scenario> probeScenarios() throws IOException {
        return scenariosWith(MealyCli.PROBE);
    }

    private static List<Scenario> scenariosWith(String argsFile) throws IOException {
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
    public void argsFilesMatchScenario(Scenario scenario) throws IOException {
        ScenarioCli.assertArgsFilesSelectScenario(scenario,
            List.of(ScenarioCli.LEARN, ScenarioCli.TEST, MealyCli.PROBE, MealyCli.IDENTIFY));

        // test checks against the scenario's own model
        Assertions.assertEquals(
            "${" + ScenarioCli.SCENARIO_DIR + "}/" + MealyModelEndpoint.MODEL_FILE,
            ScenarioCli.optionValue(scenario.requireFile(ScenarioCli.TEST), "-testSpecification"),
            () -> "-testSpecification of " + ScenarioCli.TEST);

        // a shorter -responseWait loses responses, so the endpoint would look nondeterministic
        for (String argsFile: List.of(ScenarioCli.LEARN, ScenarioCli.TEST, MealyCli.IDENTIFY)) {
            long responseWait = Long
                .parseLong(ScenarioCli.optionValue(scenario.requireFile(argsFile), "-responseWait"));
            Assertions.assertTrue(responseWait >= scenario.getLatency(), () -> "-responseWait " + responseWait
                + " of " + argsFile + " is below the latency " + scenario.getLatency());
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("learnScenarios")
    public void learnsScenarioModel(Scenario scenario, LearnedMealyScenarios learned) throws IOException {
        LearnedMealyScenarios.Learning learning = learned.learning(scenario);
        if (learning.failure() != null) {
            Assertions.fail("the command line threw", learning.failure());
        }
        Path outputDir = learning.run().getOutputDir();
        ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = learning.result();

        Assertions.assertTrue(result.hasLearnerResult(), "no learner result, see the log for the cause");
        Assertions.assertFalse(result.getLearnerResult().isEmpty(),
            "learning produced no result, see the log for the cause");
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.STATISTICS_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.MAPPER_CONNECTION_CONFIG_FILENAME)),
            "default mapper connection config not copied");

        ScenarioCli.assertArgsKept(learning.learn(), outputDir);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("learnScenarios")
    public void learnedModelIsEquivalent(Scenario scenario, LearnedMealyScenarios learned) {
        LearnedMealyScenarios.Learning learning = learned.learning(scenario);
        Assumptions.assumeTrue(learning.learned(), () -> "learning " + scenario + " failed, see learnsScenarioModel");
        MealyAssertions.assertLearnedScenarioModel(learning.run(), learning.result().getLearnerResult());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testScenarios")
    public void testRunnerMatchesModel(Scenario scenario, @TempDir Path workDir) throws IOException {
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
    public void timingProbeFindsLatency(Scenario scenario, @TempDir Path workDir) throws IOException {
        MealyScenarioRun run = new MealyScenarioRun(scenario, workDir);
        run.writeTests();
        MealyCliBuilder builder = new MealyCliBuilder();

        builder.process(run.command(MealyCli.PROBE));

        Assertions.assertEquals(1, builder.getProbedTimes().size(), "timing probe not run, see the log for the cause");
        MealyAssertions.assertProbedLatency(scenario, builder.getProbedTimes().get(0));
    }
}
