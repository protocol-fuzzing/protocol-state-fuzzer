package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioRun;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The run of a Mealy scenario: its work directory, with the scenario's
 * alphabet listing the inputs of its model, and the model to check the run's
 * results against.
 */
public final class MealyScenarioRun extends ScenarioRun {

    /** The classpath directory of the Mealy scenarios, in {@code <protocol>/<scenario>/}. */
    public static final String SCENARIOS = "e2e/mealy";

    private static final Map<Path, CompactMealy<String, String>> MODELS = new ConcurrentHashMap<>();

    private final CompactMealy<String, String> expectedModel;

    /**
     * Prepares the work directory of a run.
     *
     * @param scenario the scenario to run
     * @param workDir  an empty directory for the run's files
     */
    public MealyScenarioRun(Scenario scenario, Path workDir) {
        super(scenario, workDir);
        this.expectedModel = model(scenario);
        writeAlphabet(expectedModel.getInputAlphabet(), workDir.resolve(ALPHABET_FILE));
    }

    public CompactMealy<String, String> getExpectedModel() {
        return expectedModel;
    }

    /**
     * Computes the outputs the scenario's model gives to a test.
     *
     * @param  test the input word
     *
     * @return      the output names
     */
    public Word<String> expectedOutputs(Word<MessageInput> test) {
        return expectedModel.computeOutput(test.transform(MessageInput::getName));
    }

    /**
     * Finds a word on which a learned model differs from the scenario's.
     *
     * @param  learned the learned model
     *
     * @return         the word, or {@code null} if the models are equivalent
     */
    public Word<String> separatingWord(MealyMachineWrapper<MessageInput, MessageOutput> learned) {
        return Automata.findSeparatingWord(expectedModel, toNames(learned), expectedModel.getInputAlphabet());
    }

    /** The tests are the input words of the model. */
    @Override
    protected List<String> testSteps() {
        return List.copyOf(expectedModel.getInputAlphabet());
    }

    /**
     * Collects the learned models of runs as fingerprinting expects them: in
     * {@code <protocol>/<role>/<scenario>/}, each with the
     * {@code learnedModel.dot} and {@code alphabet.xml} written by learning.
     *
     * @param runs           the learned runs
     * @param fingerprintDir the directory to collect the models in
     */
    public static void collectLearnedModels(Collection<MealyScenarioRun> runs, Path fingerprintDir) {
        for (MealyScenarioRun run: runs) {
            try {
                Path collected = Files.createDirectories(fingerprintDir.resolve(run.getScenario().getGroup().path())
                    .resolve(run.getScenario().getName()));
                for (String file: List.of(StateFuzzer.LEARNED_MODEL_FILENAME, ALPHABET_FILE)) {
                    Files.copy(run.getOutputDir().resolve(file), collected.resolve(file));
                }
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Returns a scenario's model, parsed once per test run and shared, so it
     * must not be modified.
     *
     * @param  scenario the scenario
     *
     * @return          the model over names
     */
    public static CompactMealy<String, String> model(Scenario scenario) {
        return MODELS.computeIfAbsent(scenario.requireFile(MealyModelEndpoint.MODEL_FILE),
            MealyModelEndpoint::parseModel);
    }

    /**
     * Returns the names of the scenarios in the same group, with the same
     * inputs and an equivalent model as a scenario, including itself.
     *
     * @param  scenario  the scenario
     * @param  scenarios the scenarios to choose from
     *
     * @return           the names of the equivalent scenarios
     */
    public static Set<String> equivalentScenarios(Scenario scenario, List<Scenario> scenarios) {
        CompactMealy<String, String> model = model(scenario);
        Set<String> equivalent = new HashSet<>();
        for (Scenario other: scenarios) {
            if (!other.getGroup().equals(scenario.getGroup())) {
                continue;
            }
            CompactMealy<String, String> otherModel = model(other);
            if (sameInputs(model, otherModel)
                && Automata.findSeparatingWord(model, otherModel, model.getInputAlphabet()) == null) {
                equivalent.add(other.getName());
            }
        }
        return equivalent;
    }

    /**
     * Returns every pair of scenarios that differential testing can compare:
     * those in the same group and with the same inputs.
     *
     * @param  scenarios the scenarios, in order
     *
     * @return           the pairs, each in scenario order
     */
    public static List<List<Scenario>> comparablePairs(List<Scenario> scenarios) {
        List<List<Scenario>> pairs = new ArrayList<>();
        for (int i = 0; i < scenarios.size(); i++) {
            for (int j = i + 1; j < scenarios.size(); j++) {
                Scenario first = scenarios.get(i);
                Scenario second = scenarios.get(j);
                if (first.getGroup().equals(second.getGroup()) && sameInputs(model(first), model(second))) {
                    pairs.add(List.of(first, second));
                }
            }
        }
        return pairs;
    }

    /**
     * Converts a learned model to one over input and output names, comparable
     * to the scenario's model.
     *
     * @param  learned the learned model
     *
     * @return         the model over names
     */
    public static CompactMealy<String, String> toNames(MealyMachineWrapper<MessageInput, MessageOutput> learned) {
        return toNames(learned.getMealyMachine(), learned.getAlphabet());
    }

    private static <S> CompactMealy<String, String> toNames(MealyMachine<S, MessageInput, ?, MessageOutput> model,
        Alphabet<MessageInput> alphabet) {
        Alphabet<String> names = new ListAlphabet<>(alphabet.stream().map(MessageInput::getName).toList());
        CompactMealy<String, String> result = new CompactMealy<>(names);

        S initial = model.getInitialState();
        if (initial == null) {
            return result;
        }
        Map<S, Integer> states = new HashMap<>();
        states.put(initial, result.addInitialState());
        Deque<S> pending = new ArrayDeque<>(List.of(initial));
        while (!pending.isEmpty()) {
            S state = pending.poll();
            for (MessageInput input: alphabet) {
                S successor = model.getSuccessor(state, input);
                MessageOutput output = model.getOutput(state, input);
                if (successor == null || output == null) {
                    continue;
                }
                Integer target = states.get(successor);
                if (target == null) {
                    target = result.addState();
                    states.put(successor, target);
                    pending.add(successor);
                }
                result.addTransition(states.get(state), input.getName(), target, output.getName());
            }
        }
        return result;
    }

    /** Checks whether two models have the same inputs, in any order. */
    private static boolean sameInputs(CompactMealy<String, String> first, CompactMealy<String, String> second) {
        return new HashSet<>(first.getInputAlphabet()).equals(new HashSet<>(second.getInputAlphabet()));
    }

    private static void writeAlphabet(Alphabet<String> inputs, Path file) {
        Alphabet<MessageInput> alphabet = new ListAlphabet<>(inputs.stream().map(MessageInput::new).toList());
        try (OutputStream outputStream = Files.newOutputStream(file)) {
            MessageAlphabetPojoXml.serializer().write(outputStream, alphabet);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        catch (AlphabetSerializerException e) {
            throw new IllegalStateException("Cannot write alphabet " + file, e);
        }
    }
}
