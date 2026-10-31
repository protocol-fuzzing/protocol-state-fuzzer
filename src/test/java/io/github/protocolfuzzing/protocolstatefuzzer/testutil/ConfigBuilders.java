package io.github.protocolfuzzing.protocolstatefuzzer.testutil;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;

/**
 * Minimal config builders, producing standard configs with no sub-configs.
 */
public final class ConfigBuilders {

    private ConfigBuilders() {}

    /**
     * Builds client and server configs with all sub-configs set to null.
     *
     * @return the state fuzzer config builder
     */
    public static StateFuzzerConfigBuilder stateFuzzer() {
        return new StateFuzzerConfigBuilder() {
            @Override
            public StateFuzzerClientConfig buildClientConfig() {
                return new StateFuzzerClientConfigStandard(null);
            }

            @Override
            public StateFuzzerServerConfig buildServerConfig() {
                return new StateFuzzerServerConfigStandard(null);
            }
        };
    }

    /**
     * Builds a {@link DiffTesterConfigStandard}.
     *
     * @return the diff tester config builder
     */
    public static DiffTesterConfigBuilder diffTester() {
        return DiffTesterConfigStandard::new;
    }

    /**
     * Builds a {@link FingerprintConfigStandard}.
     *
     * @return the fingerprint config builder
     */
    public static FingerprintConfigBuilder fingerprint() {
        return FingerprintConfigStandard::new;
    }
}
