package io.github.protocolfuzzing.protocolstatefuzzer.testutil;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;

import java.util.function.Supplier;

/**
 * Config builders for tests that parse command-line arguments.
 */
public final class ConfigBuilders {

    private ConfigBuilders() {}

    /**
     * Builds client and server configs with all sub-configs set to null.
     *
     * @return the state fuzzer config builder
     */
    public static StateFuzzerConfigBuilder stateFuzzer() {
        return stateFuzzer(() -> new StateFuzzerClientConfigStandard(null),
            () -> new StateFuzzerServerConfigStandard(null));
    }

    /**
     * Builds client and server configs with the given suppliers, called on every build so that each parse gets
     * fresh configs.
     *
     * @param  client the supplier of client configs
     * @param  server the supplier of server configs
     *
     * @return        the state fuzzer config builder
     */
    public static StateFuzzerConfigBuilder stateFuzzer(Supplier<StateFuzzerClientConfig> client,
        Supplier<StateFuzzerServerConfig> server) {

        return new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return client.get();
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return server.get();
            }
        };
    }

    /**
     * Builds a {@link DiffTesterConfigStandard}.
     *
     * @return the diff tester config builder
     */
    public static DiffTesterConfigBuilder diffTester() {
        return new DiffTesterConfigBuilder() {
            @Override
            public DiffTesterConfig buildConfig() {
                return new DiffTesterConfigStandard();
            }
        };
    }

    /**
     * Builds a {@link FingerprintConfigStandard}.
     *
     * @return the fingerprint config builder
     */
    public static FingerprintConfigBuilder fingerprint() {
        return new FingerprintConfigBuilder() {
            @Override
            public FingerprintConfig buildFingerprintConfig() {
                return new FingerprintConfigStandard();
            }
        };
    }
}
