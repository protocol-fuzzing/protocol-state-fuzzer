package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester;

import java.util.List;

/**
 * Used to store the result of differential testing of two Mealy Machine models.
 * <p>
 * An empty DiffTestResul is used to indicate an error dring the differential test process.
 * A normal DiffTestResult can be converted to empty using {@link #toEmpty()}.
 * If the result is not empty and {@link #getDivergences()} returns an empty list,
 * the two models are considered equivalent.
 */
public class DiffTestResult {

    /** Stores the list of divergences found between the two models */
    private List<DivergenceRecord<String, String>> divergences;

    /** Stores the name of the first model */
    private String modelAName;

    /** Stores the name of the second model */
    private String modelBName;

    /**
     * Constructs a new instance with the given list of divergences.
     *
     * @param divergences the list of divergeneces found between the two models
     * @param modelAName  the name of the first model
     * @param modelBName  the name of the second model
     */
    public DiffTestResult(List<DivergenceRecord<String, String>> divergences, String modelAName, String modelBName) {
        this.divergences = divergences;
        this.modelAName = modelAName;
        this.modelBName = modelBName;
    }

    /**
     * Returns the list of divergences found between the two models.
     * <p>
     * Default value: null if the instance is empty.
     *
     * @return the list of divergences found between the two models
     */
    public List<DivergenceRecord<String, String>> getDivergences() {
        return divergences;
    }

    /**
     * Returns the name of the first model.
     *
     * @return the name of the first model
     */
    public String getModelAName() {
        return this.modelAName;
    }

    /**
     * Returns the name of the second model.
     *
     * @return the name of the second model
     */
    public String getModelBName() {
        return this.modelBName;
    }

    /**
     * Returns a reference to the same instance, initializing every parameter to null
     * <p>
     * The {@code get} methods will return null after this.
     *
     * @return a reference to the same instance
     */
    public DiffTestResult toEmpty() {
        this.divergences = null;
        this.modelAName = null;
        this.modelBName = null;
        return this;
    }

    /**
     * Checks if this instance is empty.
     *
     * @return {@code true} if this instance is empty
     */
    public boolean isEmpty() {
        return divergences == null;
    }

    /**
     * Checks if the two models are equivalent.
     * <p>
     * The models are considered equivalent if no divergences were found,
     * meaning the divergences list is non-null and empty.
     *
     * @return {@code true} if no divergences were found between the two models
     */
    public boolean isEquivalent() {
        return divergences != null && divergences.isEmpty();
    }
}
