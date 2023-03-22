package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config;

public interface StateFuzzerConfigBuilder {
    StateFuzzerClientConfig buildClientConfig();
    StateFuzzerServerConfig buildServerConfig();
}
