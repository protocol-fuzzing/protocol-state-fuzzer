package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioRun;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTestResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DivergenceRecord;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintNode;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The checks of the Mealy end-to-end tests.
 */
public final class MealyAssertions {

    private MealyAssertions() {}

    /**
     * Checks that learning produced a model equivalent to the scenario's.
     *
     * @param run    the scenario run
     * @param result the learner result
     */
    public static void assertLearnedScenarioModel(MealyScenarioRun run,
        LearnerResult<MealyMachineWrapper<MessageInput, MessageOutput>> result) {
        Assertions.assertFalse(result.isEmpty(), "learning produced no result, see the log for the cause");
        Word<String> separatingWord = run.separatingWord(result.getLearnedModel());
        Assertions.assertNull(separatingWord, () -> "learned model differs on " + separatingWord + ": expected "
            + run.getExpectedModel().computeOutput(separatingWord) + ", learned "
            + MealyScenarioRun.toNames(result.getLearnedModel()).computeOutput(separatingWord));
    }

    /**
     * Checks the results of running every input word up to
     * {@value ScenarioRun#TEST_LENGTH}: one result per word, with each word run
     * {@code -times} times (from the scenario's {@value ScenarioCli#TEST} args
     * file), all runs giving the same output, and that output matching both
     * the scenario's model and the test specification.
     *
     * @param run           the scenario run
     * @param results       the test runner results
     * @param specification the test specification loaded by the test runner
     */
    public static void assertTestResultsMatchModel(MealyScenarioRun run,
        List<TestRunnerResult<Word<MessageInput>, Word<MessageOutput>>> results,
        MealyMachine<?, MessageInput, ?, MessageOutput> specification) {
        Assertions.assertNotNull(specification, "test specification not loaded");

        // one test per input word up to TEST_LENGTH
        Assertions.assertEquals(ScenarioRun.testCount(run.getExpectedModel().getInputAlphabet().size()),
            results.size(), "number of tests run");

        for (TestRunnerResult<Word<MessageInput>, Word<MessageOutput>> result: results) {
            Word<MessageInput> test = result.getInputWord();
            Map<Word<MessageOutput>, Integer> outputs = result.getGeneratedOutputs();
            Assertions.assertEquals(run.testTimes(), outputs.values().stream().mapToInt(Integer::intValue).sum(),
                () -> "number of runs of " + test);
            Assertions.assertEquals(1, outputs.size(), () -> "nondeterministic outputs for " + test + ": " + outputs);

            Word<MessageOutput> output = outputs.keySet().iterator().next();
            Assertions.assertEquals(run.expectedOutputs(test), output.transform(MessageOutput::getName),
                () -> "outputs for " + test);
            Assertions.assertEquals(specification.computeOutput(test), output,
                () -> "test specification outputs for " + test);
        }
    }

    /**
     * Checks that the timing probe found a {@code responseWait} within the
     * {@code -probeTol} of the scenario's {@value MealyCli#PROBE} args file
     * above the scenario's latency.
     *
     * @param scenario the scenario
     * @param times    the times found by the timing probe
     */
    public static void assertProbedLatency(Scenario scenario, Map<String, Integer> times) {
        Integer responseWait = times.get("responseWait");
        Assertions.assertNotNull(responseWait, () -> "no responseWait in " + times);
        int tolerance = Integer.parseInt(ScenarioCli.optionValue(scenario.requireFile(MealyCli.PROBE), "-probeTol"));
        Assertions.assertTrue(
            responseWait >= scenario.getLatency() && responseWait <= scenario.getLatency() + tolerance,
            () -> "responseWait " + responseWait + " not within " + tolerance + " ms above the latency of "
                + scenario.getLatency() + " ms");
    }

    /**
     * Checks that differential testing found no divergences.
     *
     * @param result the differential testing result
     */
    public static void assertNoDivergences(DiffTestResult result) {
        Assertions.assertFalse(result.isEmpty(), "differential testing failed, see the log for the cause");
        Assertions.assertEquals(List.of(), result.getDivergences());
        Assertions.assertTrue(result.areModelsEquivalent());
    }

    /**
     * Checks the divergences found between the models learned from two
     * scenarios. There are some if and only if the scenario models differ,
     * each is a real difference between them, and the shortest is a shortest
     * separating word.
     *
     * @param first  the first scenario, model A
     * @param second the second scenario, model B
     * @param result the differential testing result
     */
    public static void assertDivergencesMatchScenarioModels(Scenario first, Scenario second, DiffTestResult result) {
        Assertions.assertFalse(result.isEmpty(), "differential testing failed, see the log for the cause");
        Assertions.assertEquals(first.getName(), result.getModelAName());
        Assertions.assertEquals(second.getName(), result.getModelBName());

        CompactMealy<String, String> firstModel = MealyScenarioRun.model(first);
        CompactMealy<String, String> secondModel = MealyScenarioRun.model(second);
        Word<String> separatingWord = Automata.findSeparatingWord(firstModel, secondModel,
            firstModel.getInputAlphabet());
        List<DivergenceRecord<String, String>> divergences = result.getDivergences();
        Assertions.assertEquals(separatingWord == null, result.areModelsEquivalent(), "models equivalent");
        if (separatingWord == null) {
            Assertions.assertEquals(List.of(), divergences, "divergences between equivalent models");
            return;
        }
        Assertions.assertFalse(divergences.isEmpty(), () -> "no divergences, but the models differ on "
            + separatingWord);

        // every divergence is a real difference, as the models agree up to its last input and differ on that
        for (DivergenceRecord<String, String> divergence: divergences) {
            Word<String> witness = Word.fromList(divergence.witnessSequence());
            Word<String> firstOutputs = firstModel.computeOutput(witness);
            Word<String> secondOutputs = secondModel.computeOutput(witness);
            String message = "divergence " + divergence + ", scenario models give " + firstOutputs + " and "
                + secondOutputs;
            Assertions.assertEquals(firstOutputs.prefix(-1), secondOutputs.prefix(-1), message);
            Assertions.assertEquals(firstOutputs.lastSymbol(), divergence.outputA(), message);
            Assertions.assertEquals(secondOutputs.lastSymbol(), divergence.outputB(), message);
            Assertions.assertNotEquals(divergence.outputA(), divergence.outputB(), message);
        }

        // the search is breadth-first, so the shortest divergence is a shortest separating word
        int shortest = divergences.stream().mapToInt(divergence -> divergence.witnessSequence().size()).min()
            .getAsInt();
        Assertions.assertEquals(separatingWord.length(), shortest, () -> "shortest divergence, expected as long as "
            + separatingWord);
    }

    /**
     * Checks that every leaf of a fingerprint holds one set of equivalent
     * models, and that every set has a leaf.
     *
     * @param scenarios   the fingerprinted scenarios
     * @param fingerprint the root of the fingerprint
     */
    public static void assertFingerprintSeparatesScenarios(List<Scenario> scenarios, FingerprintNode fingerprint) {
        Assertions.assertNotNull(fingerprint, "fingerprinting failed, see the log for the cause");
        Set<Set<String>> expected = new HashSet<>();
        for (Scenario scenario: scenarios) {
            expected.add(MealyScenarioRun.equivalentScenarios(scenario, scenarios));
        }

        Set<Set<String>> leaves = new HashSet<>();
        Deque<FingerprintNode> pending = new ArrayDeque<>(List.of(fingerprint));
        while (!pending.isEmpty()) {
            FingerprintNode node = pending.poll();
            if (node.isLeaf()) {
                leaves.add(Set.copyOf(node.getModels()));
            } else {
                pending.addAll(node.getChildren().values());
            }
        }
        Assertions.assertEquals(expected, leaves,
            "every leaf of the fingerprint should hold one set of equivalent models");
    }
}
