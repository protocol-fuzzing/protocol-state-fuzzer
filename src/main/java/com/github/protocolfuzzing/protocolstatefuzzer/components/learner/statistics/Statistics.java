package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Statistics collected over the learning process.
 */
public class Statistics {
    protected String runDescription;
    protected int alphabetSize;
    protected int states;
    protected long learnTests;
    protected long learnInputs;
    protected long allTests;
    protected long allInputs;
    protected List<DefaultQuery<?, ?>> counterexamples;
    protected long duration;
    protected long lastHypTests;
    protected long lastHypInputs;
    protected boolean finished;
    protected List<HypothesisStatistics> hypStats;
    protected String reason;

    protected Statistics() {
        runDescription = "";
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        export(sw);
        return sw.toString();
    }

    public void export(Writer writer) {
        PrintWriter out = new PrintWriter(writer);
        out.println(runDescription);
        out.println("=== STATISTICS ===");
        out.println("Learning finished: " + finished);
        if (!finished) {
            out.println("Reason: " + reason);
        }
        out.println("Size of the input alphabet: " + alphabetSize);
        out.println("Number of states: " + states);
        out.println("Number of hypotheses: " + hypStats.size());
        out.println("Number of inputs: " + allInputs);
        out.println("Number of tests: " + allTests);
        out.println("Number of learning inputs: " + learnInputs);
        out.println("Number of learning tests: " + learnTests);
        out.println("Number of inputs up to last hypothesis: " + lastHypInputs);
        out.println("Number of tests up to last hypothesis: " + lastHypTests);
        out.println("Time (ms) to learn model: " + duration);
        out.println("Counterexamples:");
        int ind = 1;
        for (Object ce : counterexamples) {
            out.println("CE " + (ind++) + ":" + ce);
        }
        if (!hypStats.isEmpty()) {
            out.println("Number of inputs when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getInputs()).toList());
            out.println("Number of tests when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getTests()).toList());
            out.println("Time (ms) when hypothesis was generated: "
                    + hypStats.stream().map(s -> s.getSnapshot().getTime()).toList());

            List<HypothesisStatistics> invalidatedHypStates = new ArrayList<>(hypStats);
            if (invalidatedHypStates.get(invalidatedHypStates.size() - 1).getCounterexample() == null) {
                invalidatedHypStates.remove(invalidatedHypStates.size() - 1);
            }

            out.println("Number of inputs when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getInputs()).toList());
            out.println("Number of tests when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getTests()).toList());
            out.println("Time (ms) when counterexample was found: "
                    + invalidatedHypStates.stream().map(s -> s.getCounterexampleSnapshot().getTime()).toList());
        }
        out.close();
    }

    protected void generateRunDescription(StateFuzzerEnabler stateFuzzerEnabler, Alphabet<?> alphabet) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        out.println("=== RUN DESCRIPTION ===");
        out.println("Alphabet: " + alphabet);
        stateFuzzerEnabler.printRunDescription(out);

        out.close();
        runDescription = sw.toString();
    }

    public String getRunDescription() {
        return runDescription;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    protected void setAlphabetSize(int alphabetSize) {
        this.alphabetSize = alphabetSize;
    }

    public int getStates() {
        return states;
    }

    protected void setStates(int states) {
        this.states = states;
    }

    public long getLearnTests() {
        return learnTests;
    }

    protected void setLearnTests(long learnTests) {
        this.learnTests = learnTests;
    }

    public long getLearnInputs() {
        return learnInputs;
    }

    protected void setLearnInputs(long learnInputs) {
        this.learnInputs = learnInputs;
    }

    public long getAllTests() {
        return allTests;
    }

    protected void setAllTests(long allTests) {
        this.allTests = allTests;
    }

    public long getAllInputs() {
        return allInputs;
    }

    public void setAllInputs(long allInputs) {
        this.allInputs = allInputs;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    protected long getLastHypTests() {
        return lastHypTests;
    }

    protected void setLastHypTests(long lastHypTests) {
        this.lastHypTests = lastHypTests;
    }

    public long getLastHypInputs() {
        return lastHypInputs;
    }

    protected void setLastHypInputs(long lastHypInputs) {
        this.lastHypInputs = lastHypInputs;
    }

    protected void setCounterexamples(List<DefaultQuery<?, ?>> counterexamples) {
        this.counterexamples = counterexamples;
    }

    protected void setFinished(boolean finished, String reason) {
        this.finished = finished;
        this.reason = reason;
    }

    public void setHypStats(List<HypothesisStatistics> hypStats) {
        this.hypStats = hypStats;
    }
}
