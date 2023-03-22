package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ProbeTestRunner extends TestRunner {

    protected List<TestRunnerResult<AbstractInput, AbstractOutput>> control = null;

    public ProbeTestRunner(TestRunnerEnabler testRunnerEnabler, AlphabetBuilder alphabetBuilder,
                           SulBuilder sulBuilder, SulWrapper sulWrapper) {
        super(testRunnerEnabler, alphabetBuilder, sulBuilder, sulWrapper);
    }

    public boolean isNonDeterministic(boolean controlRun) throws IOException {
        List<TestRunnerResult<AbstractInput, AbstractOutput>> results = super.runTests();
        Iterator<TestRunnerResult<AbstractInput, AbstractOutput>> itControl = null;

        if (!controlRun) {
            itControl = control.iterator();
        }

        for (TestRunnerResult<AbstractInput, AbstractOutput> result : results) {
            if (result.getGeneratedOutputs().size() > 1) {
                return true;
            }
            if (itControl != null && !(result.getGeneratedOutputs().equals(itControl.next().getGeneratedOutputs()))) {
                return true;
            }
        }

        if (controlRun) {
            control = results;
        }

        return false;
    }
}
