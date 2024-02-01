package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.RandomWpMethodEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.SampledTestsEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.WpSampledTestsEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestParser;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.MealyWMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.data.Constants;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.equivalence.IOEquivalenceOracle;
import de.learnlib.ralib.equivalence.IORandomWalk;
import de.learnlib.ralib.learning.ralambda.RaLambda;
import de.learnlib.ralib.oracles.DataWordOracle;
import de.learnlib.ralib.oracles.SDTLogicOracle;
import de.learnlib.ralib.oracles.SimulatorOracle;
import de.learnlib.ralib.oracles.TreeOracleFactory;
import de.learnlib.ralib.oracles.mto.MultiTheorySDTLogicOracle;
import de.learnlib.ralib.oracles.mto.MultiTheoryTreeOracle;
import de.learnlib.ralib.solver.ConstraintSolver;
import de.learnlib.ralib.sul.DataWordSUL;
import de.learnlib.ralib.theory.Theory;
import de.learnlib.ralib.words.ParameterizedSymbol;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Factory for creating Learning Setup components.
 */
public class LearningSetupFactory {

    /**
     * Create a new MealyLearner from the given parameters.
     *
     * @param config    the learner configuration to be used
     * @param sulOracle the sul oracle to be used for the Learner
     * @param alphabet  the (input) alphabet to be used
     * @return the created Learner
     */
    public static MealyLearner<AbstractInput, AbstractOutput> createMealyLearner(
            LearnerConfig config,
            MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
            Alphabet<AbstractInput> alphabet) {

        return switch (config.getLearningAlgorithm()) {
            case LSTAR ->
                new ExtensibleLStarMealy<>(alphabet, sulOracle, new ArrayList<>(),
                        ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_SHORTEST);

            case TTT ->
                new TTTLearnerMealyBuilder<AbstractInput, AbstractOutput>()
                        .withAlphabet(alphabet)
                        .withOracle(sulOracle)
                        .withAnalyzer(AcexAnalyzers.BINARY_SEARCH_FWD)
                        .create();

            case RS ->
                new ExtensibleLStarMealy<>(alphabet, sulOracle, new ArrayList<>(),
                        ObservationTableCEXHandlers.RIVEST_SCHAPIRE, ClosingStrategies.CLOSE_SHORTEST);

            case KV ->
                new KearnsVaziraniMealy<>(alphabet, sulOracle, false, AcexAnalyzers.LINEAR_FWD);

            default ->
                throw new RuntimeException("Learner algorithm " + config.getLearningAlgorithm() + " is not supported");
        };
    }

    /**
     * Create a new MealyLearner from the given parameters.
     *
     * @param config    the learner configuration to be used
     * @param sulOracle the sul oracle to be used for the Learner
     * @param alphabet  the (input) alphabet to be used
     * @return the created Learner
     */
    public static RaLambda createRALearner(
            LearnerConfigRA config,
            DataWordOracle dwOracle,
            Alphabet<ParameterizedSymbol> alphabet,
            Map<DataType, Theory> teachers,
            ConstraintSolver solver,
            Constants consts) {

        ParameterizedSymbol[] inputs = (ParameterizedSymbol[]) alphabet.toArray();
        MultiTheoryTreeOracle mto = new MultiTheoryTreeOracle(dwOracle, teachers, consts, solver);

        SDTLogicOracle slo = new MultiTheorySDTLogicOracle(consts, solver);

        TreeOracleFactory hypFactory = (RegisterAutomaton hyp) -> new MultiTheoryTreeOracle(new SimulatorOracle(hyp),
                teachers, consts, solver);

        return switch (config.getLearningAlgorithm()) {
            case RALAMBDA ->
                new RaLambda(mto, hypFactory, slo, consts, config.getIOMode(), inputs);

            case RASTAR ->
                throw new RuntimeException("Not implemented");

            default ->
                throw new RuntimeException(
                        "RA Learner algorithm " + config.getLearningAlgorithm() + " is not supported");
        };
    }

    /**
     * Create a new Equivalence Oracle from the given parameters.
     *
     * @param config    the learner configuration to be used
     * @param sul       the sul that is contained inside the sulOracle
     * @param sulOracle the sul oracle to be used that contains the sul
     * @param alphabet  the alphabet to be used
     * @return the created Equivalence Oracle
     */
    public static EquivalenceOracle<MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>> createEquivalenceOracle(
            LearnerConfig config,
            SUL<AbstractInput, AbstractOutput> sul,
            MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
            Alphabet<AbstractInput> alphabet) {

        if (config.getEquivalenceAlgorithms().isEmpty()) {
            return (m, i) -> null;
        }

        if (config.getEquivalenceAlgorithms().size() == 1) {
            return createEquivalenceOracleForAlgorithm(config.getEquivalenceAlgorithms().get(0), config, sul, sulOracle,
                    alphabet);
        }

        List<EquivalenceOracle.MealyEquivalenceOracle<AbstractInput, AbstractOutput>> eqOracles;

        eqOracles = config.getEquivalenceAlgorithms().stream()
                .map(alg -> createEquivalenceOracleForAlgorithm(alg, config, sul, sulOracle, alphabet))
                .collect(Collectors.toList());

        return new MealyEQOracleChain<>(eqOracles);
    }

    /**
     * Create one or more new RA Equivalence Oracles from the given parameters.
     *
     * @param config   the learner configuration to be used
     * @param sul      the sul that is contained inside the sulOracle
     * @param dwOracle the sul oracle to be used that contains the sul
     * @param alphabet the alphabet to be used
     * @return the created RA Equivalence Oracle(s)
     */
    public static IOEquivalenceOracle createEquivalenceOracle(
            LearnerConfigRA config,
            DataWordSUL sul,
            DataWordOracle dwOracle,
            Alphabet<ParameterizedSymbol> alphabet,
            Map<DataType, Theory> teachers,
            Constants consts) {

        if (config.getEquivalenceAlgorithms().isEmpty()) {
            throw new RuntimeException("No RA Equivalence algorithm has been chosen");
        }

        return createEquivalenceOracleForAlgorithm(config.getEquivalenceAlgorithms().get(0), config, sul, dwOracle,
        alphabet, teachers, consts);
    }

    /**
     * Create a new Equivalence Oracle for the Equivalence algorithm specified
     * and the given parameters.
     * <p>
     * The sul parameter is needed, because it cannot be extracted from the
     * sulOracle parameter.
     *
     * @param algorithm the Equivalence algorithm name
     * @param config    the learner configuration to be used
     * @param sul       the sul that is contained inside the sulOracle
     * @param sulOracle the sul oracle to be used that contains the sul
     * @param alphabet  the alphabet to be used
     * @return the created Equivalence Oracle
     */
    protected static EquivalenceOracle.MealyEquivalenceOracle<AbstractInput, AbstractOutput> createEquivalenceOracleForAlgorithm(
            EquivalenceAlgorithmName algorithm,
            LearnerConfig config,
            SUL<AbstractInput, AbstractOutput> sul,
            MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
            Alphabet<AbstractInput> alphabet) {

        return switch (algorithm) {
            // simplest method, but doesn't perform well for large models
            case RANDOM_WALK ->
                new RandomWalkEQOracle<>(sul, config.getProbReset(), config.getEquivQueryBound(), true,
                        new Random(config.getSeed()));

            // Smarter methods: state coverage, trying to distinguish states, etc.
            case W_METHOD ->
                new MealyWMethodEQOracle<>(sulOracle, config.getMaxDepth());

            case WP_METHOD ->
                new MealyWpMethodEQOracle<>(sulOracle, config.getMaxDepth());

            case RANDOM_WP_METHOD ->
                new RandomWpMethodEQOracle<>(
                        sulOracle, config.getMinLength(), config.getRandLength(),
                        config.getEquivQueryBound(), config.getSeed());

            case SAMPLED_TESTS ->
                new SampledTestsEQOracle<>(readTests(config, alphabet), sulOracle);

            case WP_SAMPLED_TESTS ->
                new WpSampledTestsEQOracle<>(
                        readTests(config, alphabet), sulOracle, config.getMinLength(),
                        config.getRandLength(), config.getSeed(), config.getEquivQueryBound());

            default ->
                throw new RuntimeException("Equivalence algorithm " + algorithm + " is not supported");
        };
    }

    /**
     * Create a new Equivalence Oracle for a RA Equivalence algorithm specified
     * and the given parameters.
     * <p>
     * The sul parameter is needed, because it cannot be extracted from the
     * sulOracle parameter.
     *
     * @param algorithm the Equivalence algorithm name
     * @param config    the learner configuration to be used
     * @param sul       the sul that is contained inside the sulOracle
     * @param dwOracle the sul oracle to be used that contains the sul
     * @param alphabet  the alphabet to be used
     * @param teachers  TODO
     * @param consts    TODO
     * @return the created Equivalence Oracle
     */
    protected static IOEquivalenceOracle createEquivalenceOracleForAlgorithm(
            EquivalenceAlgorithmName algorithm,
            LearnerConfigRA config,
            DataWordSUL sul,
            DataWordOracle dwOracle,
            Alphabet<ParameterizedSymbol> alphabet,
            Map<DataType, Theory> teachers,
            Constants consts) {
        ParameterizedSymbol[] inputs = (ParameterizedSymbol[]) alphabet.toArray();
        return switch (algorithm) {
            case IO_RANDOM_WALK ->
                new IORandomWalk(new Random(config.getSeed()),
                        sul,
                        config.getDrawSymbolsUniformly(),
                        config.getProbReset(),
                        config.getProbNewDataValue(),
                        config.getMaxRuns(),
                        config.getMaxDepthRA(),
                        consts,
                        config.getResetRuns(),
                        config.getSeedTransitions(),
                        teachers,
                        inputs);

            default ->
                throw new RuntimeException("Equivalence algorithm " + algorithm + " is not supported for RA");
        };
    }

    /**
     * Reads tests from the file found in {@link LearnerConfig#getTestFile()}.
     *
     * @param config   the learner config to be used
     * @param alphabet the alphabet of the tests
     * @return the list of words of inputs; one word for each test read
     */
    protected static List<Word<AbstractInput>> readTests(LearnerConfig config, Alphabet<AbstractInput> alphabet) {
        try {
            return new TestParser().readTests(alphabet, config.getTestFile());
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read tests from file " + config.getTestFile() + ": " + e.getMessage());
        }
    }
}
