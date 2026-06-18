package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.config.FingerprintConfigStandard;

public class FingerprintConfigBuilderSimple implements FingerprintConfigBuilder {
    @Override
    public FingerprintConfig buildConfigFing() {
        return new FingerprintConfigStandard();
    }
}
