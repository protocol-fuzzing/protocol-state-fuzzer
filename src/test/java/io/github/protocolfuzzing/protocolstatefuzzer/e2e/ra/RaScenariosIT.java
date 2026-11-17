package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioRun;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import net.automatalib.word.Word;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs each RA scenario under {@code src/test/resources/e2e/ra} through the
 * command line, with the args files in its {@code args/} directory.
 * See {@link ScenarioCli}.
 */
public class RaScenariosIT {

    @TempDir
    static Path workDir;

    /** The learning of each scenario, by scenario id. */
    private static final Map<String, Learning> LEARNINGS = new HashMap<>();

    static List<Scenario> scenarios() throws IOException {
        return Scenario.discover(RaScenarioRun.SCENARIOS);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void hasArgsFiles(Scenario scenario) {
        for (String argsFile: List.of(ScenarioCli.LEARN, ScenarioCli.TEST)) {
            Assertions.assertTrue(scenario.findFile(argsFile).isPresent(), () -> "no " + argsFile);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void argsFilesSelectOwnScenario(Scenario scenario) throws IOException {
        ScenarioCli.assertArgsFilesSelectScenario(scenario, List.of(ScenarioCli.LEARN, ScenarioCli.TEST));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void learnsScenarioModel(Scenario scenario) throws IOException {
        Learning learning = learning(scenario);
        if (learning.failure() != null) {
            Assertions.fail("the command line threw", learning.failure());
        }
        Assertions.assertTrue(learning.learned(), "no learned model, see the log for the cause");

        Path outputDir = learning.run().getOutputDir();
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.LEARNED_MODEL_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.STATISTICS_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.MAPPER_CONNECTION_CONFIG_FILENAME)),
            "default mapper connection config not copied");

        ScenarioCli.assertArgsKept(learning.learn(), outputDir);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void learnedModelIsEquivalent(Scenario scenario) {
        Learning learning = learning(scenario);
        Assumptions.assumeTrue(learning.learned(), () -> "learning " + scenario + " failed, see learnsScenarioModel");

        Assertions.assertNull(
            learning.run().findCounterexample(
                learning.result().getLearnerResult().getLearnedModel().getRegisterAutomaton()),
            "the learned model differs from model.xml on this word");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void testRunnerMatchesModel(Scenario scenario, @TempDir Path workDir) throws IOException {
        RaScenarioRun run = new RaScenarioRun(scenario, workDir);
        run.writeTests();
        RaCliBuilder builder = new RaCliBuilder();

        ProcessResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> result = builder
            .process(run.command(ScenarioCli.TEST)).get(0);

        Assertions.assertTrue(result.hasLearnerResult() && result.getLearnerResult().isFromTest(),
            "no test result, see the log for the cause");
        Assertions.assertEquals(1, builder.getTestRuns().size(), "test runner not run, see the log for the cause");
        List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> results = builder.getTestRuns().get(0);
        // one step per input and assignment of the test values to its parameters
        int steps = run.getModel().inputs().stream()
            .mapToInt(input -> (int) Math.pow(RaScenarioRun.TEST_VALUES.size(), input.getArity())).sum();
        Assertions.assertEquals(ScenarioRun.testCount(steps), results.size(), "number of tests run");
        for (TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>> testResult: results) {
            Map<Word<PSymbolInstance>, Integer> outputs = testResult.getGeneratedOutputs();
            Assertions.assertEquals(run.testTimes(), outputs.values().stream().mapToInt(Integer::intValue).sum(),
                () -> "number of runs of " + testResult.getInputWord());
            Assertions.assertEquals(1, outputs.size(), () -> "nondeterministic outputs " + outputs.keySet()
                + " for " + testResult.getInputWord());
            Assertions.assertEquals(run.expectedOutputs(testResult.getInputWord()), outputs.keySet().iterator().next(),
                () -> "outputs for " + testResult.getInputWord());
        }
    }

    /**
     * The learning of a scenario.
     *
     * @param run     the run, whose output directory holds the learned model
     * @param learn   the learning command
     * @param result  the result of the command, or null if it threw
     * @param failure what the command threw, or null
     */
    private record Learning(RaScenarioRun run, List<String> learn,
        ProcessResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> result,
        Exception failure) {

        boolean learned() {
            return failure == null && result.hasLearnerResult() && !result.getLearnerResult().isEmpty();
        }
    }

    /** Returns the learning of a scenario, done once on first use and shared by the tests. */
    private static synchronized Learning learning(Scenario scenario) {
        return LEARNINGS.computeIfAbsent(scenario.getId(), id -> {
            RaScenarioRun run = null;
            List<String> learn = List.of();
            try {
                run = new RaScenarioRun(scenario, Files.createDirectories(workDir.resolve(id)));
                learn = List.of(run.command(ScenarioCli.LEARN));
                return new Learning(run, learn, new RaCliBuilder().process(learn.toArray(String[]::new)).get(0), null);
            }
            catch (IOException | RuntimeException e) {
                return new Learning(run, learn, null, e);
            }
        });
    }
}
