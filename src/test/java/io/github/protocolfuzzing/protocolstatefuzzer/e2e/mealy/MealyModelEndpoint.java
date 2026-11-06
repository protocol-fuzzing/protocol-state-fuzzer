package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.transport.Endpoint;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.MealyDotParser;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An endpoint that executes a Mealy machine.
 * <p>
 * The model's inputs and outputs are message names, sent on the wire as
 * plain text. Each received message takes the transition of that input and
 * replies with its output, except for these outputs:
 * <ul>
 * <li>{@value OutputBuilder#TIMEOUT}: no reply</li>
 * <li>{@value OutputBuilder#SOCKET_CLOSED}: no reply, and the endpoint closes
 * the connection</li>
 * </ul>
 * Messages not in the model's input alphabet are ignored.
 * <p>
 * A message the endpoint sends on connect, typically by a client, is given as
 * the label {@code - / MESSAGE} of the start edge
 * ({@code __start0 -> s0 [label="- / MESSAGE"]}). It is not a transition of
 * the model: the DOT parser ignores it and learned models do not contain it.
 */
public final class MealyModelEndpoint implements Endpoint {

    public static final String MODEL_FILE = "model.dot";

    private static final Pattern START_EDGE = Pattern.compile("__start0\\s*->\\s*[^\\s\\[;]+\\s*\\[([^\\]]*)\\]");
    private static final Pattern LABEL = Pattern.compile("label\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern INITIAL_MESSAGE = Pattern.compile("\\s*-\\s*/\\s*(\\S+)\\s*");

    private final CompactMealy<String, String> model;
    private final String initialMessage;
    private Integer state;
    private boolean closed;

    /**
     * Creates an endpoint executing the given model.
     *
     * @param model          the complete model to execute; once it outputs
     *                           {@value OutputBuilder#SOCKET_CLOSED}, it must
     *                           keep doing so
     * @param initialMessage the message sent on connect, or null for none
     */
    public MealyModelEndpoint(CompactMealy<String, String> model, String initialMessage) {
        validate(model);
        this.model = model;
        this.initialMessage = initialMessage;
        reset();
    }

    public static MealyModelEndpoint fromScenario(Scenario scenario) {
        Path dotFile = scenario.requireFile(MODEL_FILE);
        return new MealyModelEndpoint(parseModel(dotFile), parseInitialMessage(dotFile).orElse(null));
    }

    /**
     * Reads the message sent on connect from the label {@code - / MESSAGE} of
     * the start edge.
     *
     * @param  dotFile the DOT file of the model
     *
     * @return         the initial message, if the start edge is labeled
     */
    public static Optional<String> parseInitialMessage(Path dotFile) {
        String dot;
        try {
            dot = Files.readString(dotFile, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Cannot read model " + dotFile, e);
        }
        // comments may contain example edges
        dot = dot.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");

        Matcher startEdge = START_EDGE.matcher(dot);
        if (!startEdge.find()) {
            return Optional.empty();
        }
        Matcher label = LABEL.matcher(startEdge.group(1));
        if (!label.find() || label.group(1).isBlank()) {
            return Optional.empty();
        }
        Matcher initialMessage = INITIAL_MESSAGE.matcher(label.group(1));
        if (!initialMessage.matches()) {
            throw new IllegalArgumentException("Start edge label \"" + label.group(1) + "\" of " + dotFile
                + " is not of the form \"- / MESSAGE\"");
        }
        return Optional.of(initialMessage.group(1));
    }

    /**
     * Parses a Mealy machine whose inputs and outputs are plain names.
     *
     * @param  dotFile the DOT file of the model
     *
     * @return         the parsed model
     */
    public static CompactMealy<String, String> parseModel(Path dotFile) {
        try (InputStream inputStream = Files.newInputStream(dotFile)) {
            return MealyDotParser.parse(new CompactMealy.Creator<String, String>(), inputStream,
                (input, output) -> Pair.of(input.trim(), output.trim())).model;
        }
        catch (IOException e) {
            throw new UncheckedIOException("Cannot read model " + dotFile, e);
        }
        catch (FormatException e) {
            throw new IllegalArgumentException("Invalid model " + dotFile, e);
        }
    }

    @Override
    public void reset() {
        state = model.getInitialState();
        closed = false;
    }

    @Override
    public Optional<byte[]> onConnect() {
        return Optional.ofNullable(initialMessage).map(MealyModelEndpoint::encode);
    }

    @Override
    public Optional<byte[]> onMessage(byte[] payload) {
        String input = new String(payload, StandardCharsets.UTF_8).trim();
        if (!model.getInputAlphabet().contains(input)) {
            return Optional.empty();
        }

        CompactTransition<String> transition = model.getTransition(state, input);
        if (transition == null) {
            throw new IllegalStateException("Model is missing input " + input + " in state " + state);
        }
        String output = model.getTransitionOutput(transition);
        state = model.getSuccessor(transition);

        return switch (output) {
            case OutputBuilder.TIMEOUT -> Optional.empty();
            case OutputBuilder.SOCKET_CLOSED -> {
                closed = true;
                yield Optional.empty();
            }
            default -> Optional.of(encode(output));
        };
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private static byte[] encode(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Checks that the model can be executed: it is complete, and a closed
     * connection stays closed.
     */
    private static void validate(CompactMealy<String, String> model) {
        if (model.getInitialState() == null) {
            throw new IllegalArgumentException("Model has no initial state");
        }
        for (Integer s: model.getStates()) {
            for (String input: model.getInputAlphabet()) {
                if (model.getTransition(s, input) == null) {
                    throw new IllegalArgumentException("Model is missing input " + input + " in state " + s);
                }
                if (OutputBuilder.SOCKET_CLOSED.equals(model.getOutput(s, input))) {
                    Integer closedState = model.getSuccessor(s, input);
                    for (String next: model.getInputAlphabet()) {
                        if (!OutputBuilder.SOCKET_CLOSED.equals(model.getOutput(closedState, next))) {
                            throw new IllegalArgumentException("Model outputs " + model.getOutput(closedState, next)
                                + " on " + next + " in state " + closedState + " after "
                                + OutputBuilder.SOCKET_CLOSED);
                        }
                    }
                }
            }
        }
    }
}
