package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParserTest;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import org.junit.Assert;

import java.util.HashMap;

public abstract class SULConfigTest {
    protected SULConfig parseAllOptionsWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder ,String[] reqArgs) {
        Long responseWait = 1L;
        HashMap<String, Long> inputResponseTimeout = new HashMap<>();
        inputResponseTimeout.put("IN_2", 2L);
        inputResponseTimeout.put("IN_3", 3L);
        String inputResponseTimeoutString = "IN_2:2,IN_3:3";
        String sulCommand = "sulCommand";
        String terminateCommand = "terminateCommand";
        String processDir = "processDir";
        ProcessLaunchTrigger processTrigger = ProcessLaunchTrigger.NEW_TEST;
        Long startWait = 4L;

        String commonArgs[] = new String[]{
            "-responseWait", String.valueOf(responseWait),
            "-inputResponseTimeout", inputResponseTimeoutString,
            "-command", sulCommand,
            "-terminateCommand", terminateCommand,
            "-processDir", processDir,
            "-redirectOutputStreams",
            "-processTrigger", processTrigger.name(),
            "-startWait", String.valueOf(startWait),
        };
        String[] partialArgs = CommandLineParserTest.concatArgs(commonArgs, reqArgs);

        SULConfig sulConfig = parseWithStandard(stateFuzzerConfigBuilder, partialArgs);

        Assert.assertNotNull(sulConfig);
        Assert.assertEquals(responseWait, sulConfig.getResponseWait());
        Assert.assertEquals(inputResponseTimeout, sulConfig.getInputResponseTimeout());
        Assert.assertEquals(sulCommand, sulConfig.getCommand());
        Assert.assertEquals(terminateCommand, sulConfig.getTerminateCommand());
        Assert.assertEquals(processDir, sulConfig.getProcessDir());
        Assert.assertTrue(sulConfig.isRedirectOutputStreams());
        Assert.assertEquals(processTrigger, sulConfig.getProcessTrigger());
        Assert.assertEquals(startWait, sulConfig.getStartWait());

        // SULConfig constructor does not allow null configs and instantiates them
        Assert.assertNotNull(sulConfig.getMapperConfig());
        Assert.assertNotNull(sulConfig.getSULAdapterConfig());

        return sulConfig;
    }

    protected void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] reqArgs) {
        String commonArgs[] = new String[]{
            "-responseWait", "responseWaitTime"
        };
        String[] partialArgs = CommandLineParserTest.concatArgs(commonArgs, reqArgs);

        assertInvalidParseWithEmpty(stateFuzzerConfigBuilder, partialArgs);
    }

    protected abstract SULConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs);
    protected abstract void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] partialArgs);
}
