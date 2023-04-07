package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.factory;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.RandomWpMethodEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.SampledTestsEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.oracles.WpSampledTestsEQOracle;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core.TestParser;
import com.google.common.collect.Lists;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.MealyWMethodEQOracle;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LearnerFactory {
    public static final long SEED = 123456L;

    public static MealyLearner<AbstractInput, AbstractOutput> loadLearner(
            LearnerConfig config, MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
            Alphabet<AbstractInput> alphabet) {
        return switch (config.getLearningAlgorithm()) {
            case LSTAR ->
                    new ExtensibleLStarMealy<>(alphabet, sulOracle, Lists.newArrayList(),
                            ObservationTableCEXHandlers.CLASSIC_LSTAR, ClosingStrategies.CLOSE_SHORTEST);
            case RS ->
                    new ExtensibleLStarMealy<>(alphabet, sulOracle, Lists.newArrayList(),
                            ObservationTableCEXHandlers.RIVEST_SCHAPIRE, ClosingStrategies.CLOSE_SHORTEST);
            case TTT ->
                    new TTTLearnerMealyBuilder<AbstractInput, AbstractOutput>()
                            .withAlphabet(alphabet)
                            .withOracle(sulOracle)
                            .withAnalyzer(AcexAnalyzers.BINARY_SEARCH_FWD)
                            .create();
            case KV ->
                    new KearnsVaziraniMealy<>(alphabet, sulOracle, false, AcexAnalyzers.LINEAR_FWD);
            default ->
                    throw new RuntimeException("Learner algorithm '" + config.getLearningAlgorithm() + "' not supported");
        };
    }

    // W_METHOD, MODIFIED_W_METHOD, WP_METHOD, RANDOM_WORDS, RANDOM_WALK, RANDOM_WP_METHOD
    public static EquivalenceOracle<
            MealyMachine<?, AbstractInput, ?, AbstractOutput>, AbstractInput, Word<AbstractOutput>
            > loadTester(LearnerConfig config, SUL<AbstractInput, AbstractOutput> sul,
                         MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
                         Alphabet<AbstractInput> alphabet) {

        if (config.getEquivalenceAlgorithms().isEmpty()) {
            return (m, i) -> null;
        } else {
            if (config.getEquivalenceAlgorithms().size() == 1) {
                return loadTesterForAlgorithm(config.getEquivalenceAlgorithms().get(0), config,
                        sul, sulOracle, alphabet);
            } else {
                List<EquivalenceOracle.MealyEquivalenceOracle<AbstractInput, AbstractOutput>> eqOracles =
                        config.getEquivalenceAlgorithms().stream()
                                .map(alg -> loadTesterForAlgorithm(alg, config, sul, sulOracle, alphabet))
                                .collect(Collectors.toList());
                return new MealyEQOracleChain<>(eqOracles);
            }
        }
    }

    protected static EquivalenceOracle.MealyEquivalenceOracle<AbstractInput, AbstractOutput>
        loadTesterForAlgorithm(EquivalenceAlgorithmName algorithm, LearnerConfig config,
                               SUL<AbstractInput, AbstractOutput> sul,
                               MealyMembershipOracle<AbstractInput, AbstractOutput> sulOracle,
                               Alphabet<AbstractInput> alphabet) {
        // simplest method, but doesn't perform well in practice, especially for large models
        return switch (algorithm) {
            case RANDOM_WALK ->
                    new RandomWalkEQOracle<>(sul, config.getProbReset(), config.getEquivQueryBound(),
                            true, new Random(config.getSeed()));
            // Other methods are somewhat smarter than random testing:
            // state coverage, trying to distinguish states, etc.
            case W_METHOD ->
                    new MealyWMethodEQOracle<>(sulOracle, config.getMaxDepth());
            case WP_METHOD ->
                    new MealyWpMethodEQOracle<>(sulOracle, config.getMaxDepth());
            case RANDOM_WP_METHOD ->
                    new RandomWpMethodEQOracle<>(sulOracle, config.getMinLength(), config.getRandLength(),
                            config.getEquivQueryBound(), config.getSeed());
            case SAMPLED_TESTS ->
                    new SampledTestsEQOracle<>(readTests(config, alphabet), sulOracle);
            case WP_SAMPLED_TESTS ->
                    new WpSampledTestsEQOracle<>(readTests(config, alphabet), sulOracle, config.getMinLength(),
                            config.getRandLength(), config.getSeed(), config.getEquivQueryBound());
            default ->
                    throw new RuntimeException("Equivalence algorithm '" + algorithm + "' not supported");
        };
    }

    protected static List<Word<AbstractInput>> readTests(LearnerConfig config, Alphabet<AbstractInput> alphabet) {
        TestParser parser = new TestParser();
        try {
            return parser.readTests(alphabet, config.getTestFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not read tests from file " + config.getTestFile(), e);
        }
    }
}
