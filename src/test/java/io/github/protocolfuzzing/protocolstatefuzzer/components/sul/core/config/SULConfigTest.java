package io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

public abstract class SULConfigTest {
    protected SULConfig parseAllOptionsWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] reqArgs) {
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

        // @formatter:off
        String commonArgs[] = new String[] {
            "-responseWait", String.valueOf(responseWait),
            "-inputResponseTimeout", inputResponseTimeoutString,
            "-command", sulCommand,
            "-terminateCommand", terminateCommand,
            "-processDir", processDir,
            "-redirectOutputStreams",
            "-processTrigger", processTrigger.name(),
            "-startWait", String.valueOf(startWait),
        };
        // @formatter:on

        String[] partialArgs = CliTestSupport.concatArgs(commonArgs, reqArgs);

        SULConfig sulConfig = parseWithStandard(stateFuzzerConfigBuilder, partialArgs);

        Assertions.assertNotNull(sulConfig);
        Assertions.assertEquals(responseWait, sulConfig.getResponseWait());
        Assertions.assertEquals(inputResponseTimeout, sulConfig.getInputResponseTimeout());
        Assertions.assertEquals(sulCommand, sulConfig.getCommand());
        Assertions.assertEquals(terminateCommand, sulConfig.getTerminateCommand());
        Assertions.assertEquals(processDir, sulConfig.getProcessDir());
        Assertions.assertTrue(sulConfig.isRedirectOutputStreams());
        Assertions.assertEquals(processTrigger, sulConfig.getProcessTrigger());
        Assertions.assertEquals(startWait, sulConfig.getStartWait());

        // SULConfig constructor does not allow null configs and instantiates them
        Assertions.assertNotNull(sulConfig.getMapperConfig());
        Assertions.assertNotNull(sulConfig.getSULAdapterConfig());

        return sulConfig;
    }

    protected void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] reqArgs) {
        String commonArgs[] = new String[] {"-responseWait", "responseWaitTime"};
        String[] partialArgs = CliTestSupport.concatArgs(commonArgs, reqArgs);

        assertInvalidParseWithEmpty(stateFuzzerConfigBuilder, partialArgs);
    }

    protected abstract SULConfig parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs);

    protected abstract void assertInvalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs);
}
