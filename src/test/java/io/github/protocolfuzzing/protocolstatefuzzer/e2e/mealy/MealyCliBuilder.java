package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.Fingerprint;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.Identifier;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.IdentifierBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.IdentifierStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config.IdentifierConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config.IdentifierEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.ProbeException;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds everything the command-line parser runs, as the builder of a real
 * fuzzer does. These are the standard configs, filled in from the arguments, and the
 * standard state fuzzer, test runner, timing probe, differential tester,
 * fingerprinting and identifier over the {@link InMemoryMealySULBuilder}.
 * <p>
 * The command line only logs the results of the test runner, the timing probe
 * and the identifier, so the ones built here also record their results.
 */
public class MealyCliBuilder implements StateFuzzerConfigBuilder,
    StateFuzzerBuilder<MealyMachineWrapper<MessageInput, MessageOutput>>, TestRunnerBuilder, TimingProbeBuilder,
    DiffTesterBuilder, FingerprintBuilder, IdentifierBuilder<MealyMachineWrapper<MessageInput, MessageOutput>> {

    private final AlphabetBuilder<MessageInput> alphabetBuilder = new AlphabetBuilderStandard<>(
        MessageAlphabetPojoXml.serializer());
    private final InMemoryMealySULBuilder sulBuilder = new InMemoryMealySULBuilder();

    private final List<TestRun> testRuns = new ArrayList<>();
    private final List<Map<String, Integer>> probedTimes = new ArrayList<>();
    private final List<Set<String>> identifiedModels = new ArrayList<>();

    /**
     * Creates a command-line parser over this builder.
     *
     * @return the command-line parser
     */
    public CommandLineParser<MealyMachineWrapper<MessageInput, MessageOutput>> parser() {
        return new CommandLineParser<>(this, ConfigBuilders.diffTester(), ConfigBuilders.fingerprint(), this, this,
            this, this, this, this);
    }

    /**
     * Processes commands in one command-line call, separated by {@code --}.
     *
     * @param  commands the commands
     *
     * @return          the results, one per command
     */
    public List<ProcessResult<MealyMachineWrapper<MessageInput, MessageOutput>>> process(String[]... commands) {
        return parser().process(ScenarioCli.chain(commands));
    }

    /** Returns each test runner run, in order. */
    public List<TestRun> getTestRuns() {
        return testRuns;
    }

    /** Returns the times found by each timing probe run, in order. */
    public List<Map<String, Integer>> getProbedTimes() {
        return probedTimes;
    }

    /** Returns the models identified by each identifier run, in order. */
    public List<Set<String>> getIdentifiedModels() {
        return identifiedModels;
    }

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(new LearnerConfigStandard(), new SULClientConfigStandard(),
            new TestRunnerConfigStandard(), new TimingProbeConfigStandard(), new IdentifierConfigStandard());
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(new LearnerConfigStandard(), new SULServerConfigStandard(),
            new TestRunnerConfigStandard(), new TimingProbeConfigStandard(), new IdentifierConfigStandard());
    }

    @Override
    public StateFuzzer<MealyMachineWrapper<MessageInput, MessageOutput>> build(
        StateFuzzerEnabler stateFuzzerEnabler) {
        return new StateFuzzerStandard<>(
            new StateFuzzerComposerStandard<>(stateFuzzerEnabler, alphabetBuilder, sulBuilder).initialize());
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        return new TestRunnerStandard<MessageInput, MessageOutput, String, MealyExecutionContext>(testRunnerEnabler,
            alphabetBuilder, sulBuilder) {
            @Override
            protected List<TestRunnerResult<Word<MessageInput>, Word<MessageOutput>>> runTests()
                throws IOException, FormatException {
                List<TestRunnerResult<Word<MessageInput>, Word<MessageOutput>>> results = super.runTests();
                // the expected outputs of the test specification are only printed
                testRuns.add(new TestRun(results, testSpec));
                return results;
            }
        }.initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        return new TimingProbeStandard<>(timingProbeEnabler, alphabetBuilder, sulBuilder) {
            @Override
            public Map<String, Integer> findDeterministicTimesValues()
                throws IOException, FormatException, ProbeException {
                Map<String, Integer> times = super.findDeterministicTimesValues();
                probedTimes.add(times);
                return times;
            }
        }.initialize();
    }

    @Override
    public DiffTester build(DiffTesterEnabler diffTesterEnabler) {
        return new DiffTesterStandard<>(diffTesterEnabler, alphabetBuilder);
    }

    @Override
    public Fingerprint build(FingerprintEnabler fingerprintEnabler) {
        return new FingerprintStandard<>(fingerprintEnabler, alphabetBuilder);
    }

    @Override
    public Identifier<MealyMachineWrapper<MessageInput, MessageOutput>> build(IdentifierEnabler identifierEnabler) {
        // the command-line parser does not initialize the identifier, which its conformance test needs
        return new IdentifierStandard<MessageInput, MessageOutput, String, MealyExecutionContext>(identifierEnabler,
            alphabetBuilder, sulBuilder) {
            @Override
            public Set<String> run() {
                Set<String> models = super.run();
                identifiedModels.add(models);
                return models;
            }
        }.initialize();
    }

    /**
     * The outcome of one test runner run.
     *
     * @param results       the results, one per test
     * @param specification the model parsed from the test specification, or
     *                          null if none is configured
     */
    public record TestRun(List<TestRunnerResult<Word<MessageInput>, Word<MessageOutput>>> results,
        MealyMachine<?, MessageInput, ?, MessageOutput> specification) {}
}
