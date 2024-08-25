package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.StateMachine;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Tracks learning related statistics during the learning process.
 *
 * @param <I>  the type of inputs
 * @param <O>  the type of outputs
 */
public class StatisticsTracker<I, O> {

    /** Stores the constructor parameter. */
    protected Counter inputCounter;

    /** Stores the constructor parameter. */
    protected Counter testCounter;

    /** Stores the Statistics instance that is being updated. */
    protected Statistics<I, O> statistics;

    /** Stores the PrintWriter instance that used for learning state logging. */
    protected PrintWriter stateWriter;

    /** Stores the equivalence inputs used for the last counterexample found. */
    protected long lastCEInputs;

    /** Stores the equivalence queries (tests) used for the last counterexample found. */
    protected long lastCETests;

    /** Time (ms) relative to the start of the learning experiment. */
    protected long startTime;

    /**
     * The states of the learning process.
     */
    protected enum State {

        /** During learning (searching for hypothesis with membership oracle). */
        REFINEMENT,

        /** During testing (searching for counterexample with equivalence oracle). */
        TESTING,

        /** After the learning process has terminated. */
        FINISHED
    }


    /**
     * Creates a new instance from the given parameters.
     *
     * @param inputCounter  counter updated on every input of membership and equivalence queries
     * @param testCounter   counter updated on every membership and equivalence query (also named test)
     */
    public StatisticsTracker(Counter inputCounter, Counter testCounter) {
        this.inputCounter = inputCounter;
        this.testCounter = testCounter;
    }

    /**
     * Enables the logging of learning states to the specified output stream
     * by initializing {@link #stateWriter}.
     *
     * @param outputStream  the stream where the learning states should be logged
     */
    public void setRuntimeStateTracking(OutputStream outputStream) {
        this.stateWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }

    /**
     * Prints, using the {@link #stateWriter}, the new learning state along with
     * state-specific details (if {@link #setRuntimeStateTracking(OutputStream)} has been called)
     * <p>
     * Should be called only after all data structures (e.g. counterexamples)
     * corresponding to the state have been updated.
     *
     * @param newState  the new state to be logged
     */
    protected void logStateChange(State newState) {
        if (stateWriter == null) {
            return;
        }

        stateWriter.printf("(%d) New State: %s %n", System.currentTimeMillis() - startTime, newState.name());
        stateWriter.flush();

        switch(newState) {
            case FINISHED -> {
                stateWriter.close();
                stateWriter = null;
            }

            case REFINEMENT -> {
                if (statistics.getCounterexamples().isEmpty()) {
                    // do nothing if no counterexamples are found yet
                    return;
                }

                DefaultQuery<I, Word<O>> lastCe = statistics.getLastCounterexample();
                if (lastCe == null) {
                    throw new RuntimeException("Could not find last counterexample");
                }

                stateWriter.printf("Refinement CE: %s %n", lastCe.getInput().toString());
                stateWriter.printf("SUL Response: %s %n", lastCe.getOutput().toString());

                HypothesisStatistics<I, O> lastHypStats = statistics.getLastHypStats();
                if (lastHypStats == null) {
                    throw new RuntimeException("Could not find last hypothesis statistics");
                }

                Word<O> hypResponse = lastHypStats.getHypothesis().getMealyMachine().computeOutput(lastCe.getInput());
                stateWriter.printf("HYP Response: %s %n", hypResponse.toString());
            }

            default -> {
                return;
            }
        }
    }

    /**
     * Should be called before the learning starts.
     *
     * @param stateFuzzerEnabler  the configuration that enables the state fuzzing
     * @param alphabet            the alphabet used for learning
     */
    public void startLearning(StateFuzzerEnabler stateFuzzerEnabler, Alphabet<I> alphabet) {
        startTime = System.currentTimeMillis();

        lastCETests = 0;
        lastCEInputs = 0;

        statistics = new Statistics<I, O>();
        statistics.setStateFuzzerEnabler(stateFuzzerEnabler);
        statistics.setAlphabet(alphabet);
        statistics.setLearnTests(0);
        statistics.setLearnInputs(0);
        statistics.setAllTests(0);
        statistics.setAllInputs(0);
        statistics.setCounterexamples(new ArrayList<>());
        statistics.setLastHypTests(0);
        statistics.setLastHypInputs(0);
        statistics.setFinished(false, null);
        statistics.setHypStats(new ArrayList<>());

        logStateChange(State.REFINEMENT);
    }

    /**
     * Should be called every time learning produces a new hypothesis.
     *
     * @param hypothesis  the new hypothesis that has been found
     */
    public void newHypothesis(StateMachine<I, O> hypothesis) {
        long lastHypTests = testCounter.getCount();
        statistics.setLastHypTests(lastHypTests);

        long newLearnTests = statistics.getLearnTests() + lastHypTests - lastCETests;
        statistics.setLearnTests(newLearnTests);


        long lastHypInputs = inputCounter.getCount();
        statistics.setLastHypInputs(lastHypInputs);

        long newLearnInputs = statistics.getLearnInputs() + lastHypInputs - lastCEInputs;
        statistics.setLearnInputs(newLearnInputs);

        HypothesisStatistics<I, O> newHypStats = new HypothesisStatistics<I, O>();
        newHypStats.setHypothesis(hypothesis);
        newHypStats.setIndex(statistics.getHypStats().size());
        newHypStats.setSnapshot(createSnapshot());
        statistics.getHypStats().add(newHypStats);

        logStateChange(State.TESTING);
    }

    /**
     * Should be called every time equivalence oracle testing produces a counterexample.
     *
     * @param counterexample  the new counterexample that has been found
     */
    public void newCounterExample(DefaultQuery<I, Word<O>> counterexample) {
        lastCETests = testCounter.getCount();
        lastCEInputs = inputCounter.getCount();

        statistics.getCounterexamples().add(counterexample);

        HypothesisStatistics<I, O> lastHypStats = statistics.getLastHypStats();

        if (lastHypStats == null) {
            throw new RuntimeException("Could not find last hypothesis statistics");
        }

        lastHypStats.setCounterexample(counterexample);
        lastHypStats.setCounterexampleSnapshot(createSnapshot());

        logStateChange(State.REFINEMENT);
    }

    /**
     * Should be called once learning finishes with a learned model or when it
     * is abruptly terminated yet statistics are desired. In the latter
     * case the last hypothesis should be provided.
     *
     * @param learnedModel       the final model that has been learned
     * @param finished           {@code true} if the learning finished successfully
     * @param notFinishedReason  the cause of failed learning, when finished is {@code false}
     */
    public void finishedLearning(StateMachine<I, O> learnedModel, boolean finished, String notFinishedReason) {
        statistics.setStates(0);
        if (learnedModel != null && learnedModel.getMealyMachine() != null) {
            statistics.setStates(learnedModel.getMealyMachine().size());
        }

        statistics.setAllTests(testCounter.getCount());
        statistics.setAllInputs(inputCounter.getCount());
        statistics.setDuration(System.currentTimeMillis() - startTime);
        statistics.setFinished(finished, notFinishedReason);

        logStateChange(State.FINISHED);
    }

    /**
     * Should be called after learning finishes and {@link #finishedLearning} has been called.
     *
     * @return  the statistics that have been tracked
     */
    public Statistics<I, O> generateStatistics() {
        statistics.generateRunDescription();
        return statistics;
    }

    /**
     * Creates a new snapshot from the current values of {@link #testCounter},
     * {@link #inputCounter} and current running time.
     *
     * @return  the current statistics snapshot
     */
    protected StatisticsSnapshot createSnapshot() {
        return new StatisticsSnapshot(testCounter.getCount(), inputCounter.getCount(), System.currentTimeMillis() - startTime);
    }
}
