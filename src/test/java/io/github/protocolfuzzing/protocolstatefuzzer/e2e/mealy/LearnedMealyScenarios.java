package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The learning of each Mealy scenario, done once per test run and shared by
 * the end-to-end tests. {@link MealyScenariosIT} checks it, and the tests
 * using the learned models skip the scenarios whose learning failed, instead
 * of failing too.
 * <p>
 * A scenario is learned on first use, whichever test comes first, through its
 * {@value ScenarioCli#LEARN} args file chained with a {@code diff-test} of the
 * learned model against the scenario's, in one command-line call. The runs are
 * kept in a temporary directory, deleted at the end of the test run.
 * <p>
 * Tests get the instance as a parameter, through {@link Resolver}.
 */
public final class LearnedMealyScenarios implements AutoCloseable {

    /**
     * The learning of a scenario.
     *
     * @param run     the run, whose output directory holds the learned model, or
     *                    null if preparing it failed
     * @param learn   the learning command, followed in the call by a {@code diff-test}
     * @param results the results of the call, one per command, or empty if it threw
     * @param failure what the call threw, or null
     */
    public record Learning(MealyScenarioRun run, List<String> learn,
        List<ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>>> results, RuntimeException failure) {

        /**
         * Checks whether learning produced a model equivalent to the
         * scenario's, which the tests using the learned model rely on.
         *
         * @return {@code true} if the learned model can be used
         */
        public boolean learnedScenarioModel() {
            if (failure != null || results.isEmpty() || !results.get(0).hasLearnerResult()) {
                return false;
            }
            LearnerResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = results.get(0)
                .getLearnerResult();
            return !result.isEmpty() && run.separatingWord(result.getLearnedModel()) == null;
        }
    }

    private final Path workDir;
    private final Map<String, Learning> learnings = new HashMap<>();

    private LearnedMealyScenarios() {
        try {
            this.workDir = Files.createTempDirectory("psf-e2e-mealy-");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the learning of a scenario, learning it on first use.
     *
     * @param  scenario the scenario
     *
     * @return          the learning
     */
    public synchronized Learning learning(Scenario scenario) {
        return learnings.computeIfAbsent(scenario.getId(), id -> learn(scenario));
    }

    /**
     * Returns the run of a scenario whose learning succeeded, or skips the
     * calling test if it failed, in which case {@link MealyScenariosIT} reports the failure.
     *
     * @param  scenario the scenario
     *
     * @return          the run, whose output directory holds the learned model
     */
    public MealyScenarioRun assumeLearned(Scenario scenario) {
        Learning learning = learning(scenario);
        Assumptions.assumeTrue(learning.learnedScenarioModel(),
            () -> "learning " + scenario + " failed, see MealyScenariosIT");
        return learning.run();
    }

    /**
     * Checks whether the learning of all the scenarios succeeded.
     *
     * @param  scenarios the scenarios
     *
     * @return           {@code true} if all were learned
     */
    public boolean allLearned(List<Scenario> scenarios) {
        return scenarios.stream().allMatch(scenario -> learning(scenario).learnedScenarioModel());
    }

    @Override
    public void close() throws IOException {
        try (Stream<Path> paths = Files.walk(workDir)) {
            for (Path path: paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    /**
     * Learns a scenario. Anything thrown, such as for a missing args file, is
     * kept as the learning's failure, so that the tests using it skip.
     */
    private Learning learn(Scenario scenario) {
        MealyScenarioRun run = null;
        List<String> learn = List.of();
        try {
            run = new MealyScenarioRun(scenario, Files.createDirectories(workDir.resolve(scenario.getId())));
            Path outputDir = run.getOutputDir();
            learn = List.of(run.command(ScenarioCli.LEARN));
            String[] diffTest = MealyCli.diffTest(scenario.requireFile(MealyModelEndpoint.MODEL_FILE), "expected",
                outputDir.resolve(StateFuzzer.LEARNED_MODEL_FILENAME), "learned",
                outputDir.resolve(MealyScenarioRun.ALPHABET_FILE));
            return new Learning(run, learn,
                List.copyOf(new MealyCliBuilder().process(learn.toArray(String[]::new), diffTest)), null);
        }
        catch (IOException e) {
            return new Learning(run, learn, List.of(), new UncheckedIOException(e));
        }
        catch (RuntimeException e) {
            return new Learning(run, learn, List.of(), e);
        }
    }

    /**
     * Resolves {@link LearnedMealyScenarios} parameters to the instance of the
     * test run.
     */
    public static final class Resolver implements ParameterResolver {

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == LearnedMealyScenarios.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                .computeIfAbsent(LearnedMealyScenarios.class, key -> new LearnedMealyScenarios(),
                    LearnedMealyScenarios.class);
        }
    }
}
