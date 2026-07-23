package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest;

import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;

public class DiffTesterConfigBuilderSimple implements DiffTesterConfigBuilder {
    @Override
    public DiffTesterConfig buildConfig() {
        return new DiffTesterConfigStandard();
    }
}
