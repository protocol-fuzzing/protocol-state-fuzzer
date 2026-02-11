package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

/**
 * Interface that provides a new port for the SUL process dynamically,
 * usually used when a launch server handles the SUL process.
 */
public interface DynamicPortProvider {

    /**
     * Returns the new port of the SUL process.
     *
     * @return  the new port of the SUL process
     */
    Integer getSULPort();
}
