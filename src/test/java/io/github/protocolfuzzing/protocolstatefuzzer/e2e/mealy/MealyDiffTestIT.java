package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;

/**
 * Runs {@code diff-test} through the command line on the models learned from
 * every two comparable Mealy scenarios, learned through the command line too;
 * see {@link MealyCli}. Pairs with a scenario whose learning failed are
 * skipped; see {@link LearnedMealyScenarios}.
 */
@ExtendWith(LearnedMealyScenarios.Resolver.class)
public class MealyDiffTestIT {

    static List<Scenario> scenarios() {
        return Scenario.discover(MealyScenarioRun.SCENARIOS);
    }

    /** Returns every pair of scenarios that can be compared. */
    static List<Arguments> comparableScenarios() {
        return MealyScenarioRun.comparablePairs(scenarios()).stream()
            .map(pair -> Arguments.of(pair.get(0), pair.get(1))).toList();
    }

    @ParameterizedTest(name = "{0} vs {1}")
    @MethodSource("comparableScenarios")
    public void divergencesMatchScenarioModels(Scenario first, Scenario second, LearnedMealyScenarios learned) {
        Path firstOutput = learned.assumeLearned(first).getOutputDir();
        Path secondOutput = learned.assumeLearned(second).getOutputDir();

        ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>> result = new MealyCliBuilder()
            .process(MealyCli.diffTest(firstOutput.resolve(StateFuzzer.LEARNED_MODEL_FILENAME), first.getName(),
                secondOutput.resolve(StateFuzzer.LEARNED_MODEL_FILENAME), second.getName(),
                firstOutput.resolve(MealyScenarioRun.ALPHABET_FILE)))
            .get(0);

        Assertions.assertTrue(result.hasDiffTestResult(), "no differential testing result");
        MealyAssertions.assertDivergencesMatchScenarioModels(first, second, result.getDiffTestResult());
    }
}
