package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import net.automatalib.automaton.transducer.MealyMachine;

import java.util.function.Supplier;

/**
 * Impements {@code Supplier<MealyMachine<?, I, ?, O>>} to customly provide external hypothesis
 *
 * @param <I> the input type parameter
 * @param <O> the output type parameter
 */
public class IdentifierAutomatonProvider<I, O> implements Supplier<MealyMachine<?, I, ?, O>> {

    /** The hypothesis to supply */
    protected MealyMachine<?, I, ?, O> hypothesis;

    /**
     * Returns the current hypothesis
     *
     * @return the current hypothesis
     */
    @Override
    public MealyMachine<?, I, ?, O> get() {
        return hypothesis;
    }

    /**
     * Update the current hypothesis
     *
     * @param mealyMachine the new hypothesis
     */
    public void update(MealyMachine<?, I, ?, O> mealyMachine) {
        hypothesis = mealyMachine;
    }

    /**
     * Default constructor with null hypothesis
     */
    public IdentifierAutomatonProvider() {
        this.hypothesis = null;
    }

    /**
     * Initialize the provider with the given hypothesis
     *
     * @param hypothesis the initial hypothesis
     */
    public IdentifierAutomatonProvider(MealyMachine<?, I, ?, O> hypothesis) {
        this.hypothesis = hypothesis;
    }
}
