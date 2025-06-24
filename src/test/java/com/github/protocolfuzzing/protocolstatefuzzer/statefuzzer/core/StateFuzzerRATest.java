package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapperStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulServerConfigStandard;
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
        public QuietStateFuzzerServerConfigStandard(LearnerConfig learnerConfig, SulServerConfig sulServerConfig,
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

        RASulBuilder raSulBuilder = new RASulBuilder(basicServerRA, teachers, new Constants());

        StateFuzzerServerConfigStandard enabler = new QuietStateFuzzerServerConfigStandard(
                new ShortRunningLearnerConfigRA(), new SulServerConfigStandard(), new TestRunnerConfigStandard(),
                new TimingProbeConfigStandard());

        RAAlphabetBuilder alphabetBuilder = new RAAlphabetBuilder(BasicServerRA.I_CONNECT, BasicServerRA.I_MSG,
                BasicServerRA.O_TIMEOUT, BasicServerRA.O_ACK);

        StateFuzzerComposerRA<ParameterizedSymbol, Object> composer = new StateFuzzerComposerRA<ParameterizedSymbol, Object>(
                enabler, alphabetBuilder, raSulBuilder, teachers);
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
        ParameterizedServerSul parameterizedServerSul = new ParameterizedServerSul();
        InputSymbol[] inputs = new InputSymbol[] { ParameterizedServerRA.I_MSG };
        Constants consts = new Constants();
        Map<DataType, Theory> teachers = new LinkedHashMap<>();
        IntegerEqualityTheory theory = new IntegerEqualityTheory(ParameterizedServerRA.MSG_ID);
        teachers.put(ParameterizedServerRA.MSG_ID, theory);
        theory.setFreshValues(true, new SULOracle(parameterizedServerSul, new OutputSymbol("OError")));

        SulBuilder<PSymbolInstance, PSymbolInstance, Object> sulBuilder = new SulBuilder<>() {
            @Override
            public AbstractSul<PSymbolInstance, PSymbolInstance, Object> buildSul(SulConfig sulConfig,
                    CleanupTasks cleanupTasks) {
                return new ParameterizedServerSul();
            }

            @Override
            public SulWrapper<PSymbolInstance, PSymbolInstance, Object> buildWrapper() {
                return new SulWrapperStandard<>();
            }

        };

        StateFuzzerServerConfigStandard enabler = new QuietStateFuzzerServerConfigStandard(
                new ShortRunningLearnerConfigRA(), new SulServerConfigStandard(), new TestRunnerConfigStandard(),
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
