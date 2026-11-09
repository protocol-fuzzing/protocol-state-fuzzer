package io.github.protocolfuzzing.protocolstatefuzzer.e2e.ra;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.sul.SULOracle;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.tools.theories.IntegerEqualityTheory;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import de.learnlib.ralib.words.ParameterizedSymbol;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.RegisterAutomatonWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DataWordSULWrapper;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.ProcessResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzer;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerComposerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.StateFuzzerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTester;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.DiffTesterEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.Fingerprint;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.FingerprintBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.Identifier;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.IdentifierBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config.IdentifierConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.identifier.core.config.IdentifierEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunner;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerRA;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestRunnerResult;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.config.TestRunnerEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbe;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.TimingProbeBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.timingprobe.config.TimingProbeEnabler;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import io.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds everything the command-line parser runs for register automata, as
 * the builder of a real RA fuzzer does. These are the standard configs with
 * {@link LearnerConfigRA}, filled in from the arguments, and the RA state
 * fuzzer and test runner over the {@link InMemoryRaSULBuilder}.
 * <p>
 * The theories check for fresh output values with an oracle over a SUL of
 * their own, connected to its own endpoint.
 * <p>
 * The timing probe, differential testing, fingerprinting and identification
 * only support Mealy machines, so building them fails. The command line only
 * logs the results of the test runner, so the one built here also records
 * them.
 */
public class RaCliBuilder implements StateFuzzerConfigBuilder,
    StateFuzzerBuilder<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>>, TestRunnerBuilder,
    TimingProbeBuilder, DiffTesterBuilder, FingerprintBuilder,
    IdentifierBuilder<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> {

    private final RaAlphabetBuilder alphabetBuilder = new RaAlphabetBuilder();

    private final List<List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>>> testRuns = new ArrayList<>();

    /**
     * Creates a command-line parser over this builder.
     *
     * @return the command-line parser
     */
    public CommandLineParser<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> parser() {
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
    public List<ProcessResult<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>>> process(
        String[]... commands) {
        return parser().process(ScenarioCli.chain(commands));
    }

    /** Returns the results of each test runner run, in order, one per test. */
    public List<List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>>> getTestRuns() {
        return testRuns;
    }

    @Override
    public StateFuzzerClientConfig buildClientConfig() {
        return new StateFuzzerClientConfigStandard(new LearnerConfigRA(), new SULClientConfigStandard(),
            new TestRunnerConfigStandard(), new TimingProbeConfigStandard(), new IdentifierConfigStandard());
    }

    @Override
    public StateFuzzerServerConfig buildServerConfig() {
        return new StateFuzzerServerConfigStandard(new LearnerConfigRA(), new SULServerConfigStandard(),
            new TestRunnerConfigStandard(), new TimingProbeConfigStandard(), new IdentifierConfigStandard());
    }

    @Override
    public StateFuzzer<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> build(
        StateFuzzerEnabler stateFuzzerEnabler) {
        Alphabet<ParameterizedSymbol> alphabet = alphabetBuilder.build(stateFuzzerEnabler.getLearnerConfig());
        InMemoryRaSULBuilder sulBuilder = new InMemoryRaSULBuilder(alphabet);
        Map<DataType, Theory> theories = theories(alphabet, sulBuilder, stateFuzzerEnabler.getSULConfig());
        return new StateFuzzerRA<>(
            new StateFuzzerComposerRA<>(stateFuzzerEnabler, alphabetBuilder, sulBuilder, theories).initialize());
    }

    @Override
    public TestRunner build(TestRunnerEnabler testRunnerEnabler) {
        InMemoryRaSULBuilder sulBuilder = new InMemoryRaSULBuilder(
            alphabetBuilder.build(testRunnerEnabler.getLearnerConfig()));
        return new TestRunnerRA<RaSymbolXml, RaExecutionContext>(testRunnerEnabler,
            alphabetBuilder.getXmlAlphabetBuilder(), alphabetBuilder, sulBuilder) {
            @Override
            protected List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> runTests()
                throws IOException, FormatException {
                List<TestRunnerResult<Word<PSymbolInstance>, Word<PSymbolInstance>>> results = super.runTests();
                testRuns.add(results);
                return results;
            }
        }.initialize();
    }

    @Override
    public TimingProbe build(TimingProbeEnabler timingProbeEnabler) {
        throw unsupported("The timing probe");
    }

    @Override
    public DiffTester build(DiffTesterEnabler diffTesterEnabler) {
        throw unsupported("Differential testing");
    }

    @Override
    public Fingerprint build(FingerprintEnabler fingerprintEnabler) {
        throw unsupported("Fingerprinting");
    }

    @Override
    public Identifier<RegisterAutomatonWrapper<ParameterizedSymbol, PSymbolInstance>> build(
        IdentifierEnabler identifierEnabler) {
        throw unsupported("Identification");
    }

    /**
     * Creates an equality theory for each data type of the alphabet, checking
     * for fresh output values with an oracle over a SUL of their own.
     */
    private static Map<DataType, Theory> theories(Alphabet<ParameterizedSymbol> alphabet,
        InMemoryRaSULBuilder sulBuilder, SULConfig sulConfig) {
        SULOracle freshValueOracle = new SULOracle(
            new DataWordSULWrapper(sulBuilder.buildSUL(sulConfig, new CleanupTasks())), new OutputSymbol("_io_err"));
        Map<DataType, Theory> theories = new LinkedHashMap<>();
        for (ParameterizedSymbol symbol: alphabet) {
            for (DataType type: symbol.getPtypes()) {
                theories.computeIfAbsent(type, t -> {
                    IntegerEqualityTheory theory = new IntegerEqualityTheory(t);
                    theory.setFreshValues(true, freshValueOracle);
                    return theory;
                });
            }
        }
        return theories;
    }

    private static UnsupportedOperationException unsupported(String command) {
        return new UnsupportedOperationException(command + " only supports Mealy machines");
    }
}
