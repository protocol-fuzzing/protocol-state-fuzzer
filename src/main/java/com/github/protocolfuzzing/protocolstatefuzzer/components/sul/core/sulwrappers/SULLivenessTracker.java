package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

/**
 * Class used for tracking the liveness of the SUL.
 * <p>
 * The same instance of the class should be shared among other
 * classes that want to monitor or change the liveness status.
 */
public class SULLivenessTracker {
    /** Indicates whether the SUL is alive. */
    protected boolean alive;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param alive  {@code true} if the SUL is initially alive
     */
    public SULLivenessTracker(boolean alive) {
        this.alive = alive;
    }

    /**
     * Returns {@code true} if the SUL is alive.
     *
     * @return  {@code true} if the SUL is alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets the liveness status of the SUL.
     *
     * @param alive  indicates if the SUL is still alive
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
