package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The wire format of the RA endpoints. A message is sent as plain text, its
 * name followed by its data values, separated by spaces, e.g. {@code SREQ 3}.
 *
 * @param <S> the type of the symbols decoded
 */
public final class RaMessages<S extends ParameterizedSymbol> {

    private final Map<String, S> symbols = new LinkedHashMap<>();

    /**
     * Creates a codec decoding the given symbols.
     *
     * @param symbols the symbols, with distinct names
     */
    public RaMessages(Collection<S> symbols) {
        symbols.forEach(symbol -> this.symbols.put(symbol.getName(), symbol));
    }

    /**
     * Encodes a message.
     *
     * @param  message the message
     *
     * @return         the text sent on the wire
     */
    public static String encode(PSymbolInstance message) {
        StringBuilder text = new StringBuilder(message.getBaseSymbol().getName());
        for (DataValue value: message.getParameterValues()) {
            text.append(' ').append(value.getValue().toPlainString());
        }
        return text.toString();
    }

    /**
     * Decodes a message of one of the symbols.
     *
     * @param  text the text received from the wire
     *
     * @return      the message, or empty if the text names none of the symbols
     *                  or has the wrong number of data values
     */
    public Optional<PSymbolInstance> decode(String text) {
        String[] parts = text.trim().split("\\s+", -1);
        S symbol = symbols.get(parts[0]);
        if (symbol == null || parts.length - 1 != symbol.getArity()) {
            return Optional.empty();
        }
        DataValue[] values = new DataValue[symbol.getArity()];
        for (int i = 0; i < values.length; i++) {
            try {
                values[i] = new DataValue(symbol.getPtypes()[i], new BigDecimal(parts[i + 1]));
            }
            catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.of(new PSymbolInstance(symbol, values));
    }
}
