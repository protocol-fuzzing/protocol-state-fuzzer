package com.github.protocolfuzzing.protocolstatefuzzer.components.learner;

import net.automatalib.alphabet.Alphabet;

import java.io.File;

public interface AbstractStateMachine<I, O> {

    abstract public void export(File graphFile);

    abstract public Alphabet<I> getAlphabet();

    abstract public AbstractStateMachine<I, O> copy();

    abstract public boolean isNull();

    abstract public int size();

    abstract public String toString();
}
