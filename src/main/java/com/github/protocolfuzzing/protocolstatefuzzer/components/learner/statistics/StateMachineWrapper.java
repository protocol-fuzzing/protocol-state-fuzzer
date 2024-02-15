package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import java.io.File;

/**
 * Interface that wraps a given state machine.
 *
 * @param <ID>  the type of the input domain
 * @param <OD>  the type of the output domain
 */
public interface StateMachineWrapper<ID, OD> {
    /**
     * Returns the size of the state machine.
     *
     * @return  the size of the state machine
     */
    int getMachineSize();

    /**
     * Computes the output of the given input.
     *
     * @param input  the input to be provided to the state machine
     * @return       the computed output
     */
    OD computeOutput(ID input);

    /**
     * Exports the state machine to the specified file.
     *
     * @param destFile  the destination file
     */
    void export(File destFile);

    /**
     * Creates a new copy of the state machine.
     *
     * @return  a new copy of the state machine
     */
    StateMachineWrapper<ID, OD> copy();
}
