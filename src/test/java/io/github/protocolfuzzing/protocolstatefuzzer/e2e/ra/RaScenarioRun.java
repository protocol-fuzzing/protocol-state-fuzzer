package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.equivalence.IOEquivalenceTest;
import de.learnlib.ralib.sul.SimulatorSUL;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetSerializerException;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioRun;
import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The run of an RA scenario. It has a work directory, with the scenario's alphabet
 * listing the inputs and outputs of its model, and the model to check the
 * run's results against.
 */
public final class RaScenarioRun extends ScenarioRun {

    /** The classpath directory of the RA scenarios, in {@code <protocol>/<scenario>/}. */
    public static final String SCENARIOS = "e2e/ra";

    /**
     * The data values of the tests, one per step of the longest test, so that
     * the tests cover all patterns of equal and unequal values. This assumes
     * that every input has at most one parameter, as in the current models.
     */
    public static final List<BigDecimal> TEST_VALUES = List.of(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TWO);

    private final RaModelEndpoint.RaModel model;

    /**
     * Prepares the work directory of a run.
     *
     * @param scenario the scenario to run
     * @param workDir  an empty directory for the run's files
     */
    public RaScenarioRun(Scenario scenario, Path workDir) {
        super(scenario, workDir);
        this.model = RaModelEndpoint.parseModel(scenario.requireFile(RaModelEndpoint.MODEL_FILE));
        writeAlphabet(model.actions(), workDir.resolve(ALPHABET_FILE));
    }

    public RaModelEndpoint.RaModel getModel() {
        return model;
    }

    /**
     * The steps are the inputs of the model, in the format of the RA test
     * runner. An input with a parameter has one step per value of
     * {@link #TEST_VALUES}, e.g. {@code SREQ[0]}.
     */
    @Override
    protected List<String> testSteps() {
        List<String> steps = new ArrayList<>();
        for (InputSymbol input: model.inputs()) {
            steps.addAll(instances(input));
        }
        return steps;
    }

    /**
     * Computes the outputs the scenario's model gives to a test.
     *
     * @param  test the input word
     *
     * @return      the outputs
     */
    public Word<PSymbolInstance> expectedOutputs(Word<PSymbolInstance> test) {
        SimulatorSUL simulator = new SimulatorSUL(model.automaton(), model.theories(), model.constants());
        WordBuilder<PSymbolInstance> outputs = new WordBuilder<>();
        simulator.pre();
        for (PSymbolInstance input: test) {
            outputs.add(simulator.step(input));
        }
        simulator.post();
        return outputs.toWord();
    }

    /**
     * Looks for an input word on which a learned model and the scenario's
     * model differ.
     *
     * @param  learned the learned model
     *
     * @return         the counterexample, or null if the models are
     *                     equivalent
     */
    public DefaultQuery<PSymbolInstance, Boolean> findCounterexample(RegisterAutomaton learned) {
        IOEquivalenceTest test = new IOEquivalenceTest(model.automaton(), model.theories(), model.constants(), false,
            model.inputs().toArray(ParameterizedSymbol[]::new));
        return test.findCounterExample(learned, null);
    }

    private static List<String> instances(InputSymbol input) {
        List<String> instances = new ArrayList<>(List.of(""));
        for (int i = 0; i < input.getArity(); i++) {
            List<String> longer = new ArrayList<>();
            for (String values: instances) {
                for (BigDecimal value: TEST_VALUES) {
                    longer.add(values.isEmpty() ? value.toPlainString() : values + "," + value.toPlainString());
                }
            }
            instances = longer;
        }
        return input.getArity() == 0
            ? List.of(input.getName())
            : instances.stream().map(values -> input.getName() + "[" + values + "]").collect(Collectors.toList());
    }

    private static void writeAlphabet(List<ParameterizedSymbol> actions, Path file) {
        RaAlphabetBuilder alphabetBuilder = new RaAlphabetBuilder();
        List<RaSymbolXml> symbols = actions.stream().map(alphabetBuilder::fromTransformedInput).toList();
        try (OutputStream outputStream = Files.newOutputStream(file)) {
            RaAlphabetBuilder.serializer().write(outputStream, new ListAlphabet<>(symbols));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        catch (AlphabetSerializerException e) {
            throw new IllegalStateException("Cannot write the alphabet", e);
        }
    }
}
