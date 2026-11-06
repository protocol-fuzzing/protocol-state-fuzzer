package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.automata.RALocation;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.Transition;
import de.learnlib.ralib.automata.output.OutputTransition;
import de.learnlib.ralib.automata.xml.RegisterAutomatonImporter;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.sul.SimulatorSUL;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.tools.theories.IntegerEqualityTheory;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.Endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An endpoint that executes a register automaton, given in RALib's XML
 * format.
 * <p>
 * Messages are sent on the wire as {@link RaMessages}, e.g. {@code SREQ 3}.
 * Each received message takes the enabled transition of that input and
 * replies with the output of the next transition, except for these outputs:
 * <ul>
 * <li>{@value OutputBuilder#TIMEOUT}: no reply</li>
 * <li>{@value OutputBuilder#SOCKET_CLOSED}: no reply, and the endpoint closes
 * the connection</li>
 * </ul>
 * Data values are integers compared for equality; a fresh output value is the
 * smallest one greater than all values of the connection so far.
 * <p>
 * Messages that are not inputs of the model, or have the wrong number of
 * values, are ignored. The endpoint sends nothing on connect.
 */
public final class RaModelEndpoint implements Endpoint {

    public static final String MODEL_FILE = "model.xml";

    private final RaMessages<InputSymbol> inputs;
    private final SimulatorSUL simulator;
    private boolean closed;

    /**
     * Creates an endpoint executing the given model.
     *
     * @param model the model to execute; in each input location, the guards of
     *                  every input must cover all data values; once it
     *                  outputs {@value OutputBuilder#SOCKET_CLOSED}, it must
     *                  keep doing so
     */
    public RaModelEndpoint(RaModel model) {
        validate(model.automaton());
        this.inputs = new RaMessages<>(model.inputs());
        this.simulator = new SimulatorSUL(model.automaton(), model.theories(), model.constants());
        reset();
    }

    public static RaModelEndpoint fromScenario(Scenario scenario) {
        return new RaModelEndpoint(parseModel(scenario.requireFile(MODEL_FILE)));
    }

    /**
     * Parses a register automaton in RALib's XML format, with an
     * {@link IntegerEqualityTheory} for each data type.
     *
     * @param  xmlFile the XML file of the model
     *
     * @return         the parsed model
     */
    public static RaModel parseModel(Path xmlFile) {
        RegisterAutomatonImporter importer;
        try (InputStream inputStream = Files.newInputStream(xmlFile)) {
            importer = new RegisterAutomatonImporter(inputStream);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Cannot read model " + xmlFile, e);
        }
        catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid model " + xmlFile, e);
        }

        Map<DataType, Theory> theories = new LinkedHashMap<>();
        for (DataType type: importer.getDataTypes()) {
            theories.put(type, new IntegerEqualityTheory(type));
        }
        return new RaModel(importer.getRegisterAutomaton(), List.copyOf(importer.getInputs()),
            List.copyOf(importer.getActions()), theories, importer.getConstants());
    }

    @Override
    public void reset() {
        simulator.post();
        simulator.pre();
        closed = false;
    }

    @Override
    public Optional<byte[]> onConnect() {
        return Optional.empty();
    }

    @Override
    public Optional<byte[]> onMessage(byte[] payload) {
        Optional<PSymbolInstance> input = inputs.decode(new String(payload, StandardCharsets.UTF_8));
        if (input.isEmpty()) {
            return Optional.empty();
        }

        PSymbolInstance output;
        try {
            output = simulator.step(input.get());
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("Model has no enabled transition for " + input.get(), e);
        }
        return switch (output.getBaseSymbol().getName()) {
            case OutputBuilder.TIMEOUT -> Optional.empty();
            case OutputBuilder.SOCKET_CLOSED -> {
                closed = true;
                yield Optional.empty();
            }
            default -> Optional.of(RaMessages.encode(output).getBytes(StandardCharsets.UTF_8));
        };
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Checks that a closed connection stays closed: after
     * {@value OutputBuilder#SOCKET_CLOSED}, every input is answered with
     * {@value OutputBuilder#SOCKET_CLOSED}.
     */
    private static void validate(RegisterAutomaton automaton) {
        for (RALocation location: automaton.getStates()) {
            for (Transition transition: location.getOut()) {
                if (transition instanceof OutputTransition && isSocketClosed(transition)) {
                    checkStaysClosed(transition.getDestination());
                }
            }
        }
    }

    private static void checkStaysClosed(RALocation closedLocation) {
        Set<RALocation> visited = new HashSet<>();
        Deque<RALocation> pending = new ArrayDeque<>(List.of(closedLocation));
        while (!pending.isEmpty()) {
            RALocation inputLocation = pending.pop();
            if (!visited.add(inputLocation)) {
                continue;
            }
            for (Transition input: inputLocation.getOut()) {
                for (Transition output: input.getDestination().getOut()) {
                    if (!isSocketClosed(output)) {
                        throw new IllegalArgumentException("Model outputs " + output.getLabel() + " on "
                            + input.getLabel() + " after " + OutputBuilder.SOCKET_CLOSED);
                    }
                    pending.push(output.getDestination());
                }
            }
        }
    }

    private static boolean isSocketClosed(Transition transition) {
        return OutputBuilder.SOCKET_CLOSED.equals(transition.getLabel().getName());
    }

    /**
     * A register automaton parsed from RALib's XML format.
     *
     * @param automaton the automaton
     * @param inputs    the input symbols
     * @param actions   the input and output symbols
     * @param theories  the theory of each data type
     * @param constants the constants
     */
    public record RaModel(RegisterAutomaton automaton, List<InputSymbol> inputs, List<ParameterizedSymbol> actions,
        Map<DataType, Theory> theories, Constants constants) {}
}
