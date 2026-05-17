package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest;

import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftester.config.DiffTesterConfigStandard;

public class DiffTesterConfigBuilderSimple implements DiffTesterConfigBuilder {
    @Override
    public DiffTesterConfig buildConfig() {
        return new DiffTesterConfigStandard();
    }
}
