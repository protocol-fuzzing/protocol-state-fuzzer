package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioRun;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import net.automatalib.word.Word;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Runs each RA scenario under {@code src/test/resources/e2e/ra} through the
 * command line, with the args files in its {@code args/} directory; see
 * {@link ScenarioCli}.
 */
public class RaScenariosIT {

    static List<Scenario> scenarios() {
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
    public void argsFilesSelectOwnScenario(Scenario scenario) {
        ScenarioCli.assertArgsFilesSelectScenario(scenario, List.of(ScenarioCli.LEARN, ScenarioCli.TEST));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void learnsScenarioModel(Scenario scenario, @TempDir Path workDir) {
        RaScenarioRun run = new RaScenarioRun(scenario, workDir);
        String[] learn = run.command(ScenarioCli.LEARN);

        ProcessResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> result = new RaCliBuilder()
            .process(learn).get(0);

        Assertions.assertTrue(result.hasLearnerResult(), "no learner result, see the log for the cause");
        LearnerResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> learned = result
            .getLearnerResult();
        Assertions.assertFalse(learned.isEmpty(), "learning produced no model, see the log for the cause");
        Assertions.assertNull(run.findCounterexample(learned.getLearnedModel().getRegisterAutomaton()),
            "the learned model differs from model.xml on this word");

        Path outputDir = run.getOutputDir();
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.LEARNED_MODEL_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.STATISTICS_FILENAME)));
        Assertions.assertTrue(Files.isRegularFile(outputDir.resolve(StateFuzzer.MAPPER_CONNECTION_CONFIG_FILENAME)),
            "default mapper connection config not copied");

        ScenarioCli.assertArgsKept(List.of(learn), outputDir);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    public void testRunnerMatchesModel(Scenario scenario, @TempDir Path workDir) {
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
}
