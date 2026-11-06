package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.words.PSymbolInstance;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;

/**
 * Checks outputs by name.
 */
public class RaOutputChecker implements OutputChecker<PSymbolInstance> {

    @Override
    public boolean hasInitialClientMessage(PSymbolInstance output) {
        return !isTimeout(output) && !isUnknown(output) && !isSocketClosed(output) && !isDisabled(output);
    }

    @Override
    public boolean isTimeout(PSymbolInstance output) {
        return is(output, OutputBuilder.TIMEOUT);
    }

    @Override
    public boolean isUnknown(PSymbolInstance output) {
        return is(output, OutputBuilder.UNKNOWN);
    }

    @Override
    public boolean isSocketClosed(PSymbolInstance output) {
        return is(output, OutputBuilder.SOCKET_CLOSED);
    }

    @Override
    public boolean isDisabled(PSymbolInstance output) {
        return is(output, OutputBuilder.DISABLED);
    }

    private static boolean is(PSymbolInstance output, String name) {
        return name.equals(output.getBaseSymbol().getName());
    }
}
