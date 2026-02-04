package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.LoggingWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.query.DefaultQuery;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceTest;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.tools.theories.IntegerEqualityTheory;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class StateFuzzerRATest {
    static class ShortRunningLearnerConfigRA extends LearnerConfigRA {
        public ShortRunningLearnerConfigRA() {
            super();
            super.maxRuns = 100;
        }
    }

    static class QuietStateFuzzerServerConfigStandard extends StateFuzzerServerConfigStandard {
        public QuietStateFuzzerServerConfigStandard(LearnerConfig learnerConfig, SULServerConfig sulServerConfig,
                TestRunnerConfig testRunnerConfig, TimingProbeConfig timingProbeConfig) {
            super(learnerConfig, sulServerConfig, testRunnerConfig, timingProbeConfig);
            super.quiet = true;
        }

    }

    @BeforeClass
    public static void adjustLogging() {
        Configurator.setLevel(LoggingWrapper.class, Level.OFF);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInferBasicServer() {
        RegisterAutomaton basicServerRA = BasicServerRA.AUTOMATON;
        InputSymbol[] inputs = new InputSymbol[] { BasicServerRA.I_CONNECT, BasicServerRA.I_MSG };
        Constants consts = new Constants();
        Map<DataType, Theory> teachers = new LinkedHashMap<>();

        RASULBuilder raSULBuilder = new RASULBuilder(basicServerRA, teachers, new Constants());

        StateFuzzerServerConfigStandard enabler = new QuietStateFuzzerServerConfigStandard(
                new ShortRunningLearnerConfigRA(), new SULServerConfigStandard(), new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard());

        RAAlphabetBuilder alphabetBuilder = new RAAlphabetBuilder(BasicServerRA.I_CONNECT, BasicServerRA.I_MSG,
                BasicServerRA.O_TIMEOUT, BasicServerRA.O_ACK);

        StateFuzzerComposerRA<ParameterizedSymbol, Object> composer = new StateFuzzerComposerRA<ParameterizedSymbol, Object>(
                enabler, alphabetBuilder, raSULBuilder, teachers);
        composer.initialize();

        StateFuzzerRA<ParameterizedSymbol, Object> fuzzer = new StateFuzzerRA<>(composer);
        LearnerResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> result = fuzzer
                .inferRegisterAutomata();
        IOEquivalenceTest test = new IOEquivalenceTest(basicServerRA, teachers, consts, false, inputs);
        DefaultQuery<PSymbolInstance, Boolean> ce = test
                .findCounterExample(result.getLearnedModel().getRegisterAutomaton(), null);
        Assert.assertNull(ce);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInferParameterizedServer() {
        RegisterAutomaton parameterizedServerRA = ParameterizedServerRA.AUTOMATON;
        ParameterizedServerSUL parameterizedServerSUL = new ParameterizedServerSUL();
        InputSymbol[] inputs = new InputSymbol[] { ParameterizedServerRA.I_MSG };
        Constants consts = new Constants();
        Map<DataType, Theory> teachers = new LinkedHashMap<>();
        IntegerEqualityTheory theory = new IntegerEqualityTheory(ParameterizedServerRA.MSG_ID);
        teachers.put(ParameterizedServerRA.MSG_ID, theory);
        theory.setFreshValues(true, new SULOracle(parameterizedServerSUL, new OutputSymbol("OError")));

        SULBuilder<PSymbolInstance, PSymbolInstance, Object> sulBuilder = new SULBuilder<>() {
            @Override
            public AbstractSUL<PSymbolInstance, PSymbolInstance, Object> buildSUL(SULConfig sulConfig,
                    CleanupTasks cleanupTasks) {
                return new ParameterizedServerSUL();
            }

            @Override
            public SULWrapper<PSymbolInstance, PSymbolInstance, Object> buildWrapper() {
                return new SULWrapperStandard<>();
            }

        };

        StateFuzzerServerConfigStandard enabler = new QuietStateFuzzerServerConfigStandard(
                new ShortRunningLearnerConfigRA(), new SULServerConfigStandard(), new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard());
        RAAlphabetBuilder alphabetBuilder = new RAAlphabetBuilder(ParameterizedServerRA.I_MSG,
                ParameterizedServerRA.O_ACK, ParameterizedServerRA.O_TIMEOUT);
        StateFuzzerComposerRA<ParameterizedSymbol, Object> composer = new StateFuzzerComposerRA<ParameterizedSymbol, Object>(
                enabler, alphabetBuilder, sulBuilder, teachers);
        composer.initialize();

        StateFuzzerRA<ParameterizedSymbol, Object> fuzzer = new StateFuzzerRA<>(composer);
        LearnerResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> result = fuzzer
                .inferRegisterAutomata();
        IOEquivalenceTest test = new IOEquivalenceTest(parameterizedServerRA, teachers, consts, false, inputs);
        DefaultQuery<PSymbolInstance, Boolean> ce = test
                .findCounterExample(result.getLearnedModel().getRegisterAutomaton(), null);
        Assert.assertNull(ce);
    }
}
