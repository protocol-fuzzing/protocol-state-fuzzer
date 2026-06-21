package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.LearnerResult;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory.LearningSetupFactory;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.CESanitizingSULOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.CachingSULOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.LoggingSULOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.ObservationTree;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SULWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SULConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.MapperOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierAdg.Node;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.config.IdentifierEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import net.automatalib.word.Word;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The standard implementation of the Identifier Interface.
 *
 * @param <I> the type of inputs
 * @param <O> the type of outputs
 * @param <P> the type of protocol messages
 * @param <E> the type of execution context
 */
public class IdentifierStandard<I, O extends MapperOutput<O, P>, P, E>
    implements SulIdentifier<MealyMachineWrapper<I, O>> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Stores the constructor parameter. */
    protected IdentifierEnabler identifierEnabler;

    /** The built alphabet using the AlphabetBuilder constructor parameter. */
    protected Alphabet<I> alphabet;

    /** The built alphabet for identification using the AlphabetBuilder constructor parameter */
    protected Alphabet<I> adgAlphabet;

    /** The alphabet builder */
    protected AlphabetBuilder<I> alphabetBuilder;

    /** The Mapper provided from the built {@link #sul}. */
    protected Mapper<I, O, E> mapper;

    /** The sul built and wrapped via SULBuilder constructor parameter. */
    protected SUL<I, O> sul;

    /** Stores the cleanup tasks of the Identifier. */
    protected CleanupTasks cleanupTasks;

    /** The suls that are built and wrapped using the SULBuilder constructor parameter for multithread conformance */
    protected List<SUL<I, O>> suls;

    /** The learner config */
    protected LearnerConfig learnerConfig;

    /** The cache used by the learning oracles. */
    protected ObservationTree<I, O> cache;

    /** The file writer of the non determinism case. */
    protected FileWriter nonDetWriter;

    /** The output for socket closed. */
    protected O socketClosedOutput;

    /** The equivalence oracle that is composed. */
    protected EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceOracle;

    /** The custom automaton provider */
    protected IdentifierAutomatonProvider<I, O> automatonProvider;

    /**
     * Constructs a new instance from the given parameters.
     * The {@link #sul} contains the wrapped (and built) sul.
     * Invoke {@link #initialize()} afterwards.
     *
     * @param identifierEnabler the configuration that enables the identifying
     * @param alphabetBuilder   the builder of the alphabet
     * @param sulBuilder        the builder of the sul
     */
    public IdentifierStandard(
        IdentifierEnabler identifierEnabler,
        AlphabetBuilder<I> alphabetBuilder,
        SULBuilder<I, O, E> sulBuilder) {
        this.identifierEnabler = identifierEnabler;
        this.learnerConfig = identifierEnabler.getLearnerConfig();
        this.alphabetBuilder = alphabetBuilder;
        LOGGER.debug("Create main alphabet from file {}", learnerConfig.getAlphabetFilename());
        this.alphabet = this.alphabetBuilder.build(learnerConfig);

        if (identifierEnabler.getIdentifierConfig().getAlphabetFilename() == null) {
            this.adgAlphabet = this.alphabet;
            LOGGER.debug("Copying alphabet to ADG alphabet");
        } else {
            LOGGER.debug("Create ADG alphabet from file {}",
                identifierEnabler.getIdentifierConfig().getAlphabetFilename());
            this.adgAlphabet = alphabetBuilder.build(identifierEnabler.getIdentifierConfig());
        }

        this.cleanupTasks = new CleanupTasks();

        // AbstractSUL<I, O, E> abstractSUL = sulBuilder.buildSUL(identifierEnabler.getSULConfig(), cleanupTasks);
        // this.mapper = abstractSUL.getMapper();
        // this.sul = sulBuilder.buildWrapper().wrap(abstractSUL).getWrappedSUL();

        this.suls = new ArrayList<>();

        // set up wrapped SUL (System Under Learning)
        SULConfig sulConfig = identifierEnabler.getSULConfig();
        for (int i = 0; i < learnerConfig.getEquivalenceThreadCount(); i++) {
            SULConfig config = (i == 0) ? sulConfig : sulConfig.cloneWithThreadId(i);
            AbstractSUL<I, O, E> abstractTempSUL = sulBuilder.buildSUL(config, cleanupTasks);

            if (i == 0) {
                // initialize the output for the socket closed
                this.socketClosedOutput = abstractTempSUL.getMapper().getOutputBuilder()
                    .buildOutputExact(OutputBuilder.SOCKET_CLOSED);
                this.mapper = abstractTempSUL.getMapper();
            }

            SULWrapper<I, O, E> sulWrapper = sulBuilder.buildWrapper();
            SUL<I, O> sul = sulWrapper
                .wrap(abstractTempSUL)
                .setTimeLimit(learnerConfig.getTimeLimit())
                .setTestLimit(learnerConfig.getTestLimit())
                .setLoggingWrapper("")
                .getWrappedSUL();

            this.suls.add(sul);
        }
        this.sul = this.suls.get(0);

        // initialize cache as observation tree
        this.cache = new ObservationTree<>();
    }

    /**
     * Initializes the instance; to be run after the constructor.
     * It checks if the identifierConfig from the identifierEnabler contains
     * any test specification that needs to be built and used.
     *
     * @return the same instance
     */
    public IdentifierStandard<I, O, P, E> initialize() {
        if (this.identifierEnabler.getIdentifierConfig().getAdgPath() == null) {
            throw new RuntimeException("No path provided for the identifier ADG, can't proceed");
        }
        // TODO the LOGGER instances should handle this, instead of passing non det writers as arguments.
        try {
            this.nonDetWriter = new FileWriter(new File(this.identifierEnabler.getOutputDir(), "non_det_identify.log"),
                StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not create non-determinism file writer");
        }

        List<O> cacheTerminatingOutputs = new ArrayList<>();
        if (!identifierEnabler.getSULConfig().getMapperConfig().isSocketClosedAsTimeout()) {
            // if socketClosed is not treated as timeout,
            // then the output corresponding to the explicit socketClosed symbol
            // is considered a terminating output in the cache
            cacheTerminatingOutputs.add(socketClosedOutput);
        }

        this.automatonProvider = new IdentifierAutomatonProvider<>();

        composeEquivalenceOracle(cacheTerminatingOutputs);
        return this;
    }

    /**
     * Returns the alphabet to be used for the SUT.
     *
     * @return the alphabet to be used for the SUT.
     */
    public Alphabet<I> getAlphabet() {
        return alphabet;
    }

    /**
     * Returns the alphabet to be used for identification.
     *
     * @return the alphabet to be used for identifiaction.
     */
    public Alphabet<I> getAdgAlphabet() {
        return adgAlphabet;
    }

    /**
     * Returns the SULConfig of the {@link #identifierEnabler}.
     *
     * @return the SULConfig of the {@link #identifierEnabler}
     */
    public SULConfig getSULConfig() {
        return identifierEnabler.getSULConfig();
    }

    /**
     * Runs the identification using {@link #runIdentification()} and cleans up using {@link #terminate()}.
     */
    @Override
    public Set<String> run() {
        Set<String> models = new HashSet<>();
        try {
            Node result = runIdentification();

            if (result.isLeaf()) {
                models = result.getModels();
                LOGGER.info("The following model(s) were identified:");
                for (String model: models) {
                    LOGGER.info(model);
                }
            } else {
                LOGGER.info("No match was found");
            }

        }
        catch (IOException | FormatException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
            terminate();
        }
        return models;
    }

    /**
     * Executes the {@link #cleanupTasks}; should be called only after all the
     * desired tests have been executed.
     */
    public void terminate() {
        cleanupTasks.execute();
    }

    /**
     * Reads the tests provided in the IdentifierConfig of {@link #identifierEnabler},
     * executes each one of them using {@link #identify(IdentifierAdg)} and collects the results.
     *
     * @return                 a list with the test results
     *
     * @throws IOException     if an error during reading occurs
     * @throws FormatException if an invalid format was encountered
     */
    protected Node runIdentification() throws IOException, FormatException {
        IdentifierAdg adg;
        String adgFile = identifierEnabler.getIdentifierConfig().getAdgPath();

        if (new File(adgFile).exists()) {
            adg = IdentifierParser.parse(adgFile);
        } else {
            LOGGER.info("File {} does not exist, cannot procced", adgFile);
            throw new IOException("File " + adgFile + " does not exist, cannot procced");
        }

        return identify(adg);
    }

    /**
     * Runs the identification process on the sul.
     *
     * @param  adg the {@link #sul}
     *
     * @return     the result of the test
     */
    protected Node identify(IdentifierAdg adg) {
        Node result = SulIdentifier.identify(adg, sul, adgAlphabet);

        return result;
    }

    @Override
    public LearnerResult<MealyMachineWrapper<I, O>> conformanceTest(String filePath) throws RuntimeException {
        MealyMachineWrapper<I, O> hyp;
        try {
            hyp = createHyp(filePath);
        }
        catch (RuntimeException e) {
            LOGGER.error("Exception while creting hypothesis Mealy Machine: {}", e.getMessage());
            return new LearnerResult<MealyMachineWrapper<I, O>>().toEmpty();
        }
        try {
            automatonProvider.update(hyp.getMealyMachine());
            return doConformanceTest(hyp);
        }
        catch (RuntimeException e) {
            LOGGER.error("Exception encountered during Conformance Test");
            throw e;
        }
        catch (Exception e) {
            LOGGER.error("Exception generated during Conformance Test: " + e.getMessage());
            throw new RuntimeException("Exception generated during Conformance Test: " + e.getMessage());
        }
        finally {
            cleanupTasks.execute();
        }

    }

    /**
     * Executes the conformance test implementation.
     *
     * @param  hyp              the hypothesis to be tested
     *
     * @return                  the corresponding LearnerResult, which can be empty if the test fails
     *
     * @throws RuntimeException if there is an exception during conformance test
     */

    protected LearnerResult<MealyMachineWrapper<I, O>> doConformanceTest(MealyMachineWrapper<I, O> hyp)
        throws RuntimeException {
        MealyMachine<?, I, ?, O> hypothesis = hyp.getMealyMachine();
        Alphabet<I> hypAlphabet = hyp.getAlphabet();
        DefaultQuery<I, Word<O>> counterExample = null;
        LearnerResult<MealyMachineWrapper<I, O>> learnerResult = new LearnerResult<>();
        learnerResult.toNormal();
        learnerResult.addHypothesis(hyp);

        try {
            LOGGER.info("Input alphabet: {}", hypAlphabet);
            LOGGER.info("Equivalence Thread Count: {}",
                learnerConfig.getEquivalenceThreadCount());
            LOGGER.info("Starting Conformance test" + System.lineSeparator());
            // exportHypothesis(hyp, new File(outputDir, "hypothesisForConformanceTest.dot"));
            counterExample = equivalenceOracle.findCounterExample(hypothesis, hypAlphabet);

        }
        catch (Exception e) {
            LOGGER.error("Exception generated during conformanceTest\n" + e);
            LOGGER.info("Conformance test failed due to an exception, the hypothesis is not correct");

            throw new RuntimeException("Exception generated during conformanceTest: " + e.getMessage());

        }

        if (counterExample == null) {
            LOGGER.info("Conformance test succeeded");
            learnerResult.setLearnedModel(hyp);
            learnerResult.setFromTest(true);
            learnerResult.setLearnedModelFile(new File(identifierEnabler.getOutputDir(), "identificationModel.dot"));
            return learnerResult;
        }

        LOGGER.info("Counterexample: " + counterExample);

        LOGGER.info("Conformance failed, the hypothesis is not correct");
        return learnerResult.toEmpty();

    }

    /**
     * Creates a hypothesis from a given dot file and alphabet file to test against the SUT.
     *
     * @param  filePath the path to the folder containing the dot model and alphabet files
     *
     * @return          a MealyMachineWrapper representing the hypothesis
     */
    private MealyMachineWrapper<I, O> createHyp(String filePath) {

        String hypPath = filePath + File.separator + "learnedModel.dot";
        // String alphabetPath = filePath + File.separator + "alphabet.xml";

        // Alphabet<I> hypAlphabet;

        if (new File(hypPath).exists()) {
            LOGGER.info("Building hypothesis from file {}", hypPath);
        } else {
            LOGGER.info("File {} does not exist, cannot build hypothesis", hypPath);
            throw new RuntimeException("File " + hypPath + " does not exist, cannot build hypothesis");
        }

        // if (new File(alphabetPath).exists()) {
        // LOGGER.debug("Create hypothesis alphabet from file {}", alphabetPath);
        // hypAlphabet = alphabetBuilder.build(new IdentifierAlphabetStore(alphabetPath));
        // LOGGER.debug("Hyp Alphabet: {}", hypAlphabet);
        // } else {
        // LOGGER.info("File {} does not exist, using the ADG alphabet for the hypothesis", alphabetPath);
        // hypAlphabet = adgAlphabet;
        // }

        try {
            LOGGER.debug("Will return alphabet: {}", this.alphabet);
            return new MealyMachineWrapper<>(ModelFactory.buildProtocolModel(hypPath,
                new MealyIOProcessor<>(this.alphabet, mapper.getOutputBuilder())), this.alphabet);
        }
        catch (IOException | FormatException e) {
            throw new RuntimeException("Could not build protocol model from test specification: " + e.getMessage());
        }
    }

    /**
     * Composes the Equivalence Oracle and stores it in the {@link #equivalenceOracle}.
     *
     * @param terminatingOutputs the terminating outputs used by the {@link CachingSULOracle}
     */
    protected void composeEquivalenceOracle(List<O> terminatingOutputs) {
        List<MembershipOracle.MealyMembershipOracle<I, O>> equivalenceSULOracles = new ArrayList<>();
        for (SUL<I, O> sul: suls) {
            MembershipOracle.MealyMembershipOracle<I, O> equivalenceSULOracle = new SULOracle<>(sul);

            // in case sanitization is enabled, we apply a CE verification wrapper
            // to check counterexamples before they are returned to the EQ oracle
            if (learnerConfig.isCeSanitization()) {
                equivalenceSULOracle = new CESanitizingSULOracle<MealyMachine<?, I, ?, O>, I, O>(
                    learnerConfig.getCeReruns(), equivalenceSULOracle, learnerConfig.isProbabilisticSanitization(),
                    nonDetWriter, automatonProvider, cache, learnerConfig.isSkipNonDetTests());
            }

            // we are adding a cache and a logging oracle
            equivalenceSULOracle = new CachingSULOracle<>(equivalenceSULOracle, cache, !learnerConfig.isCacheTests(),
                terminatingOutputs);
            equivalenceSULOracle = new LoggingSULOracle<>(equivalenceSULOracle);
            equivalenceSULOracles.add(equivalenceSULOracle);
        }

        this.equivalenceOracle = LearningSetupFactory.createEquivalenceOracle(learnerConfig, suls,
            equivalenceSULOracles, alphabet);
    }

}
