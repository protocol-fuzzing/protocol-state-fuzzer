package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    private static final Alphabet<String> CLIENT_HELLO_FINISHED_ALPHABET = Alphabets.fromArray("CLIENT_HELLO",
        "FINISHED");

    private static final Alphabet<String> MULTI_HELLO_ALPHABET = Alphabets.fromArray(
        "CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "CLIENT_HELLO_4", "CLIENT_HELLO_5", "FINISHED");

    private static final Alphabet<String> PSK_ALPHABET = Alphabets.fromArray(
        "HELLO_VERIFY_REQUEST", "PSK_SERVER_HELLO", "SERVER_HELLO_DONE", "CHANGE_CIPHER_SPEC", "FINISHED",
        "APPLICATION", "Alert(WARNING,CLOSE_NOTIFY)", "Alert(FATAL,UNEXPECTED_MESSAGE)");

    private static final Alphabet<String> DHE_ECDHE_RSA_ALPHABET = Alphabets.fromArray(
        "HELLO_VERIFY_REQUEST", "ECDH_SERVER_HELLO",
        "ECDH_SERVER_KEY_EXCHANGE", "DH_SERVER_HELLO",
        "DH_SERVER_KEY_EXCHANGE", "RSA_SERVER_HELLO",
        "RSA_SIGN_CERTIFICATE_REQUEST",
        "RSA_FIXED_ECDH_CERTIFICATE_REQUEST",
        "RSA_FIXED_DH_CERTIFICATE_REQUEST", "DSS_SIGN_CERTIFICATE_REQUEST",
        "DSS_FIXED_DH_CERTIFICATE_REQUEST", "ECDSA_SIGN_CERTIFICATE_REQUEST",
        "SERVER_HELLO_DONE", "CHANGE_CIPHER_SPEC", "FINISHED", "APPLICATION", "CERTIFICATE",
        "EMPTY_CERTIFICATE", "Alert(WARNING,CLOSE_NOTIFY)", "Alert(FATAL,UNEXPECTED_MESSAGE)");

    private static final Alphabet<String> DHE_ECDHE_RSA_WTIH_HELLO_REQUEST = Alphabets.fromArray(
        "HELLO_VERIFY_REQUEST", "ECDH_SERVER_HELLO",
        "ECDH_SERVER_KEY_EXCHANGE", "DH_SERVER_HELLO",
        "DH_SERVER_KEY_EXCHANGE", "RSA_SERVER_HELLO",
        "HELLO_REQUEST", "RSA_SIGN_CERTIFICATE_REQUEST",
        "RSA_FIXED_ECDH_CERTIFICATE_REQUEST",
        "RSA_FIXED_DH_CERTIFICATE_REQUEST", "DSS_SIGN_CERTIFICATE_REQUEST",
        "DSS_FIXED_DH_CERTIFICATE_REQUEST", "ECDSA_SIGN_CERTIFICATE_REQUEST",
        "SERVER_HELLO_DONE", "CHANGE_CIPHER_SPEC", "FINISHED", "APPLICATION", "CERTIFICATE",
        "EMPTY_CERTIFICATE", "Alert(WARNING,CLOSE_NOTIFY)", "Alert(FATAL,UNEXPECTED_MESSAGE)");

    @Test
    public void simpleIdenticalModels_noDivergence() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", CLIENT_HELLO_FINISHED_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_base.dot", CLIENT_HELLO_FINISHED_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, CLIENT_HELLO_FINISHED_ALPHABET);

        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleModel_divergenceAtDepth0() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", CLIENT_HELLO_FINISHED_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_divergence_depth0.dot",
            CLIENT_HELLO_FINISHED_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, CLIENT_HELLO_FINISHED_ALPHABET);

        assertEquals(1, result.size());
        DivergenceRecord<String, String> divergence = result.get(0);

        assertEquals(List.of("CLIENT_HELLO"), divergence.witnessSequence());
        assertEquals("SERVER_HELLO", divergence.outputA());
        assertEquals("TEST_DIVERGENCE", divergence.outputB());
    }

    @Test
    public void simpleModel_divergenceAtDepth1() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", CLIENT_HELLO_FINISHED_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_divergence_depth1.dot",
            CLIENT_HELLO_FINISHED_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, CLIENT_HELLO_FINISHED_ALPHABET);

        assertEquals(1, result.size());
        DivergenceRecord<String, String> divergence = result.get(0);

        assertEquals(List.of("CLIENT_HELLO", "FINISHED"), divergence.witnessSequence());
        assertEquals("CHANGE_CIPHER_SPEC", divergence.outputA());
        assertEquals("ALERT_FATAL", divergence.outputB());
    }

    @Test
    public void simpleModel_divergenceAtDepthFour() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("simple_5state_base.dot", MULTI_HELLO_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_5state_divergence_depth4.dot",
            MULTI_HELLO_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, MULTI_HELLO_ALPHABET);

        assertEquals(1, result.size());
        DivergenceRecord<String, String> divergence = result.get(0);

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "CLIENT_HELLO_4", "FINISHED"),
            divergence.witnessSequence());
        assertEquals("ALERT_FATAL", divergence.outputA());
        assertEquals("SERVER_HELLO", divergence.outputB());
    }

    @Test
    public void asymmetricEquivalentModels_noDivergence() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("modelAsym_A.dot", CLIENT_HELLO_FINISHED_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("modelAsym_B.dot", CLIENT_HELLO_FINISHED_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, CLIENT_HELLO_FINISHED_ALPHABET);

        assertTrue(result.isEmpty());
    }

    @Test
    public void missingTransitionsInBothModels_divergencesFound() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("simple_2state_base.dot", CLIENT_HELLO_FINISHED_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("simple_2state_missing_transition.dot",
            CLIENT_HELLO_FINISHED_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, CLIENT_HELLO_FINISHED_ALPHABET);

        assertEquals(2, result.size());

        DivergenceRecord<String, String> d1 = result.get(0);
        DivergenceRecord<String, String> d2 = result.get(1);

        assertEquals(List.of("FINISHED"), d1.witnessSequence());
        assertNull(d1.outputA());
        assertEquals("CHANGE_CIPHER_SPEC", d1.outputB());

        assertEquals(List.of("CLIENT_HELLO", "FINISHED"), d2.witnessSequence());
        assertEquals("CHANGE_CIPHER_SPEC", d2.outputA());
        assertNull(d2.outputB());
    }

    @Test
    public void multipleDivergences_correctWitnessSequences() throws Exception {
        MealyMachine<?, String, ?, String> modelD = loadModel("simple_5state_base.dot", MULTI_HELLO_ALPHABET);
        MealyMachine<?, String, ?, String> modelI = loadModel("simple_5state_3divergences.dot", MULTI_HELLO_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelD, modelI, MULTI_HELLO_ALPHABET);

        assertEquals(3, result.size());

        DivergenceRecord<String, String> d1 = result.get(0);
        DivergenceRecord<String, String> d2 = result.get(1);
        DivergenceRecord<String, String> d3 = result.get(2);

        assertEquals(List.of("CLIENT_HELLO_1", "FINISHED"), d1.witnessSequence());
        assertEquals("CHANGE_CIPHER_SPEC", d1.outputA());
        assertEquals("SERVER_HELLO", d1.outputB());

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "FINISHED"),
            d2.witnessSequence());
        assertEquals("CHANGE_CIPHER_SPEC", d2.outputA());
        assertEquals("SERVER_HELLO", d2.outputB());

        assertEquals(List.of("CLIENT_HELLO_1", "CLIENT_HELLO_2", "CLIENT_HELLO_3", "CLIENT_HELLO_4", "FINISHED"),
            d3.witnessSequence());
        assertEquals("ALERT_FATAL", d3.outputA());
        assertEquals("SERVER_HELLO", d3.outputB());
    }

    @Test
    public void identicalModels_noDivergences() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("gnutls-3.6.7_client_psk_rwalk.dot", PSK_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("gnutls-3.6.7_client_psk_rwalk.dot", PSK_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, PSK_ALPHABET);

        assertTrue(result.isEmpty());
    }

    @Test
    public void sameComplexDtlsModel_noDivergences() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("dtls_386states_base.dot",
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);
        MealyMachine<?, String, ?, String> modelB = loadModel("dtls_386states_base.dot",
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB,
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);

        assertTrue(result.isEmpty());
    }

    @Test
    public void complexDtlsModel_oneDivergenceInState386() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("dtls_386states_base.dot",
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);
        MealyMachine<?, String, ?, String> modelB = loadModel("dtls_386states_divergence_state386.dot",
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB,
            DHE_ECDHE_RSA_WTIH_HELLO_REQUEST);

        assertEquals(1, result.size());
    }

    @Test
    public void gnutlsVsMbedtlsPsk_returnExpectedDivergences() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("gnutls-3.6.7_client_psk_rwalk.dot", PSK_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("mbedtls-2.16.1_client_psk_rwalk.dot", PSK_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, PSK_ALPHABET);

        assertEquals(37, result.size());
    }

    @Test
    public void gnutlsVsMbedtlsPsk_withOutputEquivalence() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("gnutls-3.6.7_client_psk_rwalk.dot", PSK_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("mbedtls-2.16.1_client_psk_rwalk.dot", PSK_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>(new DtlsOutputEquivalence());
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, PSK_ALPHABET);

        assertEquals(27, result.size());
    }

    @Test
    public void wolfsslDifferentVersions_returnsExpectedDivergences() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("wolfssl-4.0.0_client_psk_rwalk.dot", PSK_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("wolfssl-4.4.0_client_psk_rwalk.dot", PSK_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, PSK_ALPHABET);

        assertEquals(2, result.size());
    }

    @Test
    public void gnutlsVsMbedtlsDheEcdheRsa_returnExpectedDivergences() throws Exception {
        MealyMachine<?, String, ?, String> modelA = loadModel("gnutls-3.6.7_client_dhe_ecdhe_rsa_cert_rwalk.dot",
            DHE_ECDHE_RSA_ALPHABET);
        MealyMachine<?, String, ?, String> modelB = loadModel("mbedtls-2.16.1_client_dhe_ecdhe_rsa_cert_rwalk.dot",
            DHE_ECDHE_RSA_ALPHABET);

        DifferentialOracle<String, String> oracle = new DifferentialOracle<>();
        List<DivergenceRecord<String, String>> result = oracle.analyse(modelA, modelB, DHE_ECDHE_RSA_ALPHABET);

        assertEquals(247, result.size());
    }
}
