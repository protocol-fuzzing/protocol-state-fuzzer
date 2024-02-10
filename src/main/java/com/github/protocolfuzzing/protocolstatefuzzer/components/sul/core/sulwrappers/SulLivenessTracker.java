package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

/**
 * TODO
 */
public class SulLivenessTracker {
    /** TODO */
    protected boolean alive;

    /**
     * TODO
     */
    public SulLivenessTracker(boolean alive) {
        this.alive = alive;
    }

    /**
     * TODO
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * TODO
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
