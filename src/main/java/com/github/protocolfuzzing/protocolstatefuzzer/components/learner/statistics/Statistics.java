package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics collected over the learning process.
 */
public class Statistics {

    /** Stores the configuration that enables state fuzzing. */
    protected StateFuzzerEnabler stateFuzzerEnabler;

    /** Stores the run description of the configuration used. */
    protected String runDescription;

    /** Stores the alphabet used. */
    protected Alphabet<?> alphabet;

    /** Stores the number of states of the learned model. */
    protected int states;

    /** Stores the number of membership queries used. */
    protected long learnTests;

    /** Stores the number of inputs used in membership queries. */
    protected long learnInputs;

    /** Stores the membership and equivalence queries used. */
    protected long allTests;

    /** Stores the inputs used in membership and equivalence queries. */
    protected long allInputs;

    /** Stores the list of counterexamples found. */
    protected List<DefaultQuery<?, ?>> counterexamples;

    /** Stores the time (ms) for the learning to finish. */
    protected long duration;

    /** Stores the number of tests up to last hypothesis.*/
    protected long lastHypTests;

    /** Stores the number of inputs up to last hypothesis.*/
    protected long lastHypInputs;

    /** Shows if the learning finished successfully. */
    protected boolean finished;

    /** Stores the cause of failed learning, in case {@link #finished} is {@code false}. */
    protected String notFinishedReason;

    /** Stores a list with statistics for all hypotheses found. */
    protected List<HypothesisStatistics> hypStats;

    /**
     * Constructs a new instance with empty {@link #runDescription}.
     */
    public Statistics() {
        runDescription = "";
    }

    /**
     * Returns the result of {@link #export(Writer)} converted to String.
     *
     * @return  the string representation of this instance
     */
    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        export(sw);
        return sw.toString();
    }

    /**
     * Exports the stored {@link #runDescription} and other statistics to the given Writer.
     *
     * @param writer  the Writer to be used for exporting
     */
    public void export(Writer writer) {
        PrintWriter pw = new PrintWriter(writer);
        pw.println(runDescription);
        pw.println("=== STATISTICS ===");
        pw.println("Learning finished: " + finished);
        if (!finished) {
            pw.println("Reason: " + notFinishedReason);
        }
        pw.println("Size of the input alphabet: " + alphabet.size());
        pw.println("Number of states: " + states);
        pw.println("Number of hypotheses: " + hypStats.size());
        pw.println("Number of inputs: " + allInputs);
        pw.println("Number of tests: " + allTests);
        pw.println("Number of learning inputs: " + learnInputs);
        pw.println("Number of learning tests: " + learnTests);
        pw.println("Number of inputs up to last hypothesis: " + lastHypInputs);
        pw.println("Number of tests up to last hypothesis: " + lastHypTests);
        pw.println("Time (ms) to learn model: " + duration);

        pw.println("Counterexamples:");
        int ind = 1;
        for (Object ce : counterexamples) {
            pw.println("CE " + ind + ":" + ce);
            ind++;
        }

        if (!hypStats.isEmpty()) {
            pw.println("Number of inputs when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getInputs()).toList());

            pw.println("Number of tests when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getTests()).toList());

            pw.println("Time (ms) when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getTime()).toList());

            List<HypothesisStatistics> invalidatedHypStates = new ArrayList<>(hypStats);
            if (invalidatedHypStates.get(invalidatedHypStates.size() - 1).getCounterexample() == null) {
                invalidatedHypStates.remove(invalidatedHypStates.size() - 1);
            }

            pw.println("Number of inputs when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getInputs()).toList());

            pw.println("Number of tests when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getTests()).toList());

            pw.println("Time (ms) when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getTime()).toList());
        }
        pw.close();
    }

    /**
     * Stores to {@link #runDescription} the inputs of the {@link #alphabet}
     * and the run description obtained from the {@link #stateFuzzerEnabler}.
     */
    public void generateRunDescription() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("=== RUN DESCRIPTION ===");
        pw.println("Alphabet: " + alphabet);
        stateFuzzerEnabler.printRunDescription(pw);

        pw.close();
        runDescription = sw.toString();
    }


    /**
     * Returns the stored value of {@link #stateFuzzerEnabler}.
     *
     * @return  the stored value of {@link #stateFuzzerEnabler}
     */
    public StateFuzzerEnabler getStateFuzzerEnabler() {
        return stateFuzzerEnabler;
    }

    /**
     * Sets the value of {@link #stateFuzzerEnabler}.
     *
     * @param stateFuzzerEnabler  the configuration that enables state fuzzing
     */
    public void setStateFuzzerEnabler(StateFuzzerEnabler stateFuzzerEnabler) {
        this.stateFuzzerEnabler = stateFuzzerEnabler;
    }

    /**
     * Returns the stored value of {@link #runDescription}.
     *
     * @return  the stored value of {@link #runDescription}
     */
    public String getRunDescription() {
        return runDescription;
    }

    /**
     * Returns the stored value of {@link #alphabet}.
     *
     * @return  the stored value of {@link #alphabet}
     */
    public Alphabet<?> getAlphabet() {
        return alphabet;
    }

    /**
     * Sets the value of {@link #alphabet}.
     *
     * @param alphabet  the alphabet used
     */
    public void setAlphabet(Alphabet<?> alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * Returns the stored value of {@link #states}.
     *
     * @return  the stored value of {@link #states}
     */
    public int getStates() {
        return states;
    }

    /**
     * Sets the value of {@link #states}.
     *
     * @param states  the number of states of the learned model
     */
    public void setStates(int states) {
        this.states = states;
    }

    /**
     * Returns the stored value of {@link #learnTests}.
     *
     * @return  the stored value of {@link #learnTests}
     */
    public long getLearnTests() {
        return learnTests;
    }

    /**
     * Sets the value of {@link #learnTests}.
     *
     * @param learnTests  the number of membership queries (or learning tests)
     */
    public void setLearnTests(long learnTests) {
        this.learnTests = learnTests;
    }

    /**
     * Returns the stored value of {@link #learnInputs}.
     *
     * @return  the stored value of {@link #learnInputs}
     */
    public long getLearnInputs() {
        return learnInputs;
    }

    /**
     * Sets the value of {@link #learnInputs}.
     *
     * @param learnInputs  the number of inputs used in membership queries
     */
    public void setLearnInputs(long learnInputs) {
        this.learnInputs = learnInputs;
    }

    /**
     * Returns the stored value of {@link #allTests}.
     *
     * @return  the stored value of {@link #allTests}
     */
    public long getAllTests() {
        return allTests;
    }

    /**
     * Sets the value of {@link #allTests}.
     *
     * @param allTests  the number of total membership and equivalence queries (or tests)
     */
    public void setAllTests(long allTests) {
        this.allTests = allTests;
    }

    /**
     * Returns the stored value of {@link #allInputs}.
     *
     * @return  the stored value of {@link #allInputs}
     */
    public long getAllInputs() {
        return allInputs;
    }

    /**
     * Sets the value of {@link #allInputs}.
     *
     * @param allInputs  the number of total inputs in membership and equivalence queries
     */
    public void setAllInputs(long allInputs) {
        this.allInputs = allInputs;
    }

    /**
     * Returns the stored value of {@link #duration}.
     *
     * @return  the stored value of {@link #duration}
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the value of {@link #duration}.
     *
     * @param duration  the time (ms) of the learning
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Returns the stored value of {@link #lastHypTests}.
     *
     * @return  the stored value of {@link #lastHypTests}
     */
    public long getLastHypTests() {
        return lastHypTests;
    }

    /**
     * Sets the value of {@link #lastHypTests}.
     *
     * @param lastHypTests  the number of tests up to last hypothesis
     */
    public void setLastHypTests(long lastHypTests) {
        this.lastHypTests = lastHypTests;
    }

    /**
     * Returns the stored value of {@link #lastHypInputs}.
     *
     * @return  the stored value of {@link #lastHypInputs}
     */
    public long getLastHypInputs() {
        return lastHypInputs;
    }

    /**
     * Sets the value of {@link #lastHypInputs}.
     *
     * @param lastHypInputs  the number of inputs up to last hypothesis
     */
    public void setLastHypInputs(long lastHypInputs) {
        this.lastHypInputs = lastHypInputs;
    }

    /**
     * Sets the value of {@link #counterexamples}.
     *
     * @param counterexamples  the list of counterexamples found
     */
    public void setCounterexamples(List<DefaultQuery<?, ?>> counterexamples) {
        this.counterexamples = counterexamples;
    }

    /**
     * Returns the stored value of {@link #counterexamples}.
     *
     * @return  the stored value of {@link #counterexamples}
     */
    public List<DefaultQuery<?, ?>> getCounterexamples() {
        return counterexamples;
    }

    /**
     * Returns the last counterexample in {@link #counterexamples} or null if not found.
     *
     * @return  the last counterexample or null if not found
     */
    public DefaultQuery<?, ?> getLastCounterexample() {
        if (counterexamples == null || counterexamples.isEmpty()) {
            return null;
        }
        return counterexamples.get(counterexamples.size() - 1);
    }

    /**
     * Sets the value of {@link #finished} and {@link #notFinishedReason}.
     *
     * @param finished           {@code true} if the learning finished successfully
     * @param notFinishedReason  the cause of failed learning, when finished is {@code false}
     */
    public void setFinished(boolean finished, String notFinishedReason) {
        this.finished = finished;
        this.notFinishedReason = notFinishedReason;
    }

    /**
     * Returns the stored value of {@link #hypStats}.
     *
     * @return  the stored value of {@link #hypStats}
     */
    public List<HypothesisStatistics> getHypStats() {
        return hypStats;
    }

    /**
     * Sets the value of {@link #hypStats}.
     *
     * @param hypStats  the list of hypothesis statistics
     */
    public void setHypStats(List<HypothesisStatistics> hypStats) {
        this.hypStats = hypStats;
    }

    /**
     * Returns the last hypothesis statistics in {@link #hypStats} or null if not found.
     *
     * @return  the last hypothesis statistics or null if not found
     */
    public HypothesisStatistics getLastHypStats() {
        if (hypStats == null || hypStats.isEmpty()) {
            return null;
        }
        return hypStats.get(hypStats.size() - 1);
    }
}
