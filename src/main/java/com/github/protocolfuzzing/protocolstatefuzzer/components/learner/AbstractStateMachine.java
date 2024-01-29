package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.automatalib.alphabet.Alphabet;

import java.io.File;

public abstract class AbstractStateMachine {
    // TODO: Should the subclasses instantiate their own private loggers, 
    // this might be data-race prone?
    // Should we use an abstract method to force logger usage, or just do it in the subclasses
    // in a way that is reasonable?
    protected static final Logger LOGGER = LogManager.getLogger();

    abstract public void export(File graphFile);
    
    // TODO: This might change depending on what happens with AbstractInput
    abstract public Alphabet<?> getAlphabet();
    abstract public AbstractStateMachine copy();
    abstract public String toString();
}
