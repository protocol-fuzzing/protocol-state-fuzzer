package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;

public class FingerprintConfigBuilderSimple implements FingerprintConfigBuilder {
    @Override
    public FingerprintConfig buildFingerprintConfig() {
        return new FingerprintConfigStandard();
    }
}
