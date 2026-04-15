package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import org.junit.Test;

import java.util.List;

public class DifferentialOracleTest {
    private String resourcePath(String filename) {
        return getClass().getClassLoader().getResource("difftest/" + filename).getPath();
    }

    private final OutputBuilder<String> stringOutputBuilder = new OutputBuilder<String>() {
        @Override
        public String buildOutputExact(String name) {
            return name;
        }
    };

    private MealyMachine<?, String, ?, String> loadModel(String filename, Alphabet<String> alphabet) throws Exception {
        MealyIOProcessor<String, String> processor = new MealyIOProcessor<>(alphabet, stringOutputBuilder);
        return ModelFactory.buildProtocolModel(resourcePath(filename), processor);
    }

    @Test
    public void simpleIdenticalModels_noDivergence() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO", "FINISHED");

        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_base.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);
        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleModel_divergenceAtDepth1() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO", "FINISHED");

        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_divergence_depth1.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);

        assertEquals(1, result.size());

        DivergenceRecord<String, String> divergence = result.get(0);

        assertEquals(List.of("CLIENT_HELLO"), divergence.getWitnessSequence());
        assertEquals("FINISHED", divergence.getDivergingInput());
        assertEquals("CHANGE_CIPHER_SPEC", divergence.getOutputA());
        assertEquals("ALERT_FATAL", divergence.getOutputB());
    }

    @Test
    public void simpleModel_divergenceAtDepth0() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO", "FINISHED");

        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_divergence_depth0.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);

        assertEquals(1, result.size());

        DivergenceRecord<String, String> divergence = result.get(0);

        assertTrue(divergence.getWitnessSequence().isEmpty());
        assertEquals("CLIENT_HELLO", divergence.getDivergingInput());
        assertEquals("SERVER_HELLO", divergence.getOutputA());
        assertEquals("TEST_DIVERGENCE", divergence.getOutputB());
    }

    @Test
    public void simpleModel_divergneceAtDepthFour() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3",
            "CLIENT_HELLO_4", "CLIENT_HELLO_5", "FINISHED");

        MealyMachine<?, String, ?, String> modelA = loadModel("simple_5state_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_5state_divergence_depth4.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);

        assertEquals(1, result.size());

        DivergenceRecord<String, String> divergence = result.get(0);

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "CLIENT_HELLO_4"),
            divergence.getWitnessSequence());
        assertEquals("FINISHED", divergence.getDivergingInput());
        assertEquals("ALERT_FATAL", divergence.getOutputA());
        assertEquals("SERVER_HELLO", divergence.getOutputB());
    }

    @Test
    public void sameRealDtlsModel_noDivergneces() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray(
            "RSA_CLIENT_HELLO",
            "RSA_CLIENT_KEY_EXCHANGE",
            "CHANGE_CIPHER_SPEC",
            "FINISHED",
            "APPLICATION",
            "Alert(WARNING,CLOSE_NOTIFY)",
            "Alert(FATAL,UNEXPECTED_MESSAGE)");

        MealyMachine<?, String, ?, String> modelA = loadModel("dtls_real.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("dtls_real.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);

        assertTrue(result.isEmpty());
    }

    @Test
    public void asymmetricEquivalentModels_noDivergence() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO", "FINISHED");

        MealyMachine<?, String, ?, String> modelA = loadModel("modelAsym_A.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("modelAsym_B.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);
        assertTrue(result.isEmpty());
    }

    @Test
    public void multipleDivergneces_correctWitnessSequences() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3",
            "CLIENT_HELLO_4", "CLIENT_HELLO_5", "FINISHED");

        MealyMachine<?, String, ?, String> modelD = loadModel("simple_5state_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelI = loadModel("simple_5state_3divergences.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelD, modelI, alphabet);

        assertEquals(3, result.size());

        DivergenceRecord<String, String> d1 = result.get(0);
        DivergenceRecord<String, String> d2 = result.get(1);
        DivergenceRecord<String, String> d3 = result.get(2);

        assertEquals(List.of("CLIENT_HELLO_1"), d1.getWitnessSequence());
        assertEquals("FINISHED", d1.getDivergingInput());
        assertEquals("CHANGE_CIPHER_SPEC", d1.getOutputA());
        assertEquals("SERVER_HELLO", d1.getOutputB());

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3"), d2.getWitnessSequence());
        assertEquals("FINISHED", d2.getDivergingInput());
        assertEquals("CHANGE_CIPHER_SPEC", d2.getOutputA());
        assertEquals("SERVER_HELLO", d2.getOutputB());

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "CLIENT_HELLO_4"),
            d3.getWitnessSequence());
        assertEquals("FINISHED", d3.getDivergingInput());
        assertEquals("ALERT_FATAL", d3.getOutputA());
        assertEquals("SERVER_HELLO", d3.getOutputB());
    }

    @Test
    public void sameComplexDtlsModel_noDivergences() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray(
            "HELLO_VERIFY_REQUEST", "ECDH_SERVER_HELLO",
            "ECDH_SERVER_KEY_EXCHANGE", "DH_SERVER_HELLO",
            "DH_SERVER_KEY_EXCHANGE", "RSA_SERVER_HELLO",
            "HELLO_REQUEST", "RSA_SIGN_CERTIFICATE_REQUEST",
            "RSA_FIXED_ECDH_CERTIFICATE_REQUEST",
            "RSA_FIXED_DH_CERTIFICATE_REQUEST", "DSS_SIGN_CERTIFICATE_REQUEST",
            "DSS_FIXED_DH_CERTIFICATE_REQUEST", "ECDSA_SIGN_CERTIFICATE_REQUEST",
            "SERVER_HELLO_DONE", "CHANGE_CIPHER_SPEC", "FINISHED", "APPLICATION", "CERTIFICATE",
            "EMPTY_CERTIFICATE", "Alert(WARNING,CLOSE_NOTIFY)", "Alert(FATAL,UNEXPECTED_MESSAGE)");

        MealyMachine<?, String, ?, String> modelA = loadModel("dtls_386states_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("dtls_386states_base.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();

        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);

        assertTrue(result.isEmpty());
    }

    @Test
    public void complexDtlsModel_oneDivergneceInState386() throws Exception {
        Alphabet<String> alphabet = Alphabets.fromArray(
            "HELLO_VERIFY_REQUEST", "ECDH_SERVER_HELLO",
            "ECDH_SERVER_KEY_EXCHANGE", "DH_SERVER_HELLO",
            "DH_SERVER_KEY_EXCHANGE", "RSA_SERVER_HELLO",
            "HELLO_REQUEST", "RSA_SIGN_CERTIFICATE_REQUEST",
            "RSA_FIXED_ECDH_CERTIFICATE_REQUEST",
            "RSA_FIXED_DH_CERTIFICATE_REQUEST", "DSS_SIGN_CERTIFICATE_REQUEST",
            "DSS_FIXED_DH_CERTIFICATE_REQUEST", "ECDSA_SIGN_CERTIFICATE_REQUEST",
            "SERVER_HELLO_DONE", "CHANGE_CIPHER_SPEC", "FINISHED", "APPLICATION", "CERTIFICATE",
            "EMPTY_CERTIFICATE", "Alert(WARNING,CLOSE_NOTIFY)", "Alert(FATAL,UNEXPECTED_MESSAGE)");

        MealyMachine<?, String, ?, String> modelA = loadModel("dtls_386states_base.dot", alphabet);
        MealyMachine<?, String, ?, String> modelB = loadModel("dtls_386states_divergence_state386.dot", alphabet);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();

        // Compute and display Oracle analysis time
        // long start = System.currentTimeMillis();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, alphabet);
        // long end = System.currentTimeMillis();

        // long milliseconds = (end - start);
        // System.out.println("Oracle analysis time on two models with 389 states, one divergence: " + milliseconds +
        // "ms");

        assertEquals(1, result.size());
    }
}
