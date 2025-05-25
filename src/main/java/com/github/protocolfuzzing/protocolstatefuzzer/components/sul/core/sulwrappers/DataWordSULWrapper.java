package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.sul.SUL;
import de.learnlib.ralib.words.PSymbolInstance;

/**
 * A wrapper that can be used as an {@code SUL<PSymbolInstance,PSymbolInstance>}
 * to DataWordSUL converter. Copied from StateFuzzerComposerRA.
 */
public class DataWordSULWrapper extends DataWordSUL {

    /** Stores the wrapped sul */
    protected SUL<PSymbolInstance, PSymbolInstance> sul;

    /**
     * Constructs a new instance from the given parameters.
     *
     * @param sul the wrapped sul
     */
    public DataWordSULWrapper(SUL<PSymbolInstance, PSymbolInstance> sul) {
        this.sul = sul;
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
    }

    @Override
    public PSymbolInstance step(PSymbolInstance in) {
        return sul.step(in);
    }
}
