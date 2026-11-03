package io.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CliTestSupport;
import io.github.protocolfuzzing.protocolstatefuzzer.entrypoints.CommandLineParser;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerClientConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerConfigBuilder;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfig;
import io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core.config.StateFuzzerServerConfigStandard;
import io.github.protocolfuzzing.protocolstatefuzzer.testutil.ConfigBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SULAdapterConfigTest {
    public static String[] SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS = new String[] {"-port", "1234"};
    public static String[] SUL_SERVER_CONFIG_STANDARD_REQ_ARGS = new String[] {"-connect", "host:1234"};

    @Test
    public void parseAllOptions_SFCstd_SULCstd_SFSstd_SULSstd() {
        parseAllOptions(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null,
                    new SULClientConfigStandard(null, new SULAdapterConfigStandard()), null, null, null),
                () -> new StateFuzzerServerConfigStandard(null,
                    new SULServerConfigStandard(null, new SULAdapterConfigStandard()), null, null, null)),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    private void parseAllOptions(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] clientReqArgs,
        String[] serverReqArgs) {
        Integer adapterPort = 1;
        String adapterAddress = "adapterAddress";

        // @formatter:off
        String[] partialArgs = new String[] {
            "-adapterPort", String.valueOf(adapterPort),
            "-adapterAddress", adapterAddress,
        };
        // @formatter:on

        SULAdapterConfig[] sulAdapterConfigs = parseWithStandard(stateFuzzerConfigBuilder, partialArgs, clientReqArgs,
            serverReqArgs);

        for (SULAdapterConfig sulAdapterConfig: sulAdapterConfigs) {
            Assertions.assertNotNull(sulAdapterConfig);
            Assertions.assertEquals(adapterPort, sulAdapterConfig.getAdapterPort());
            Assertions.assertEquals(adapterAddress, sulAdapterConfig.getAdapterAddress());
        }
    }

    private SULAdapterConfig[] parseWithStandard(StateFuzzerConfigBuilder stateFuzzerConfigBuilder,
        String[] partialArgs, String[] clientReqArgs, String[] serverReqArgs) {

        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        SULAdapterConfig[] sulAdapterConfigs = new SULAdapterConfig[2];

        clientReqArgs = clientReqArgs == null ? new String[0] : clientReqArgs;
        String[] clientPartialArgs = CliTestSupport.concatArgs(partialArgs, clientReqArgs);
        StateFuzzerClientConfig clientConfig = CliTestSupport.parseClientArgs(commandLineParser,
            clientPartialArgs);
        Assertions.assertNotNull(clientConfig);
        Assertions.assertNotNull(clientConfig.getSULConfig());
        sulAdapterConfigs[0] = clientConfig.getSULConfig().getSULAdapterConfig();

        serverReqArgs = serverReqArgs == null ? new String[0] : serverReqArgs;
        String[] serverPartialArgs = CliTestSupport.concatArgs(partialArgs, serverReqArgs);
        StateFuzzerServerConfig serverConfig = CliTestSupport.parseServerArgs(commandLineParser,
            serverPartialArgs);
        Assertions.assertNotNull(serverConfig);
        Assertions.assertNotNull(serverConfig.getSULConfig());
        sulAdapterConfigs[1] = serverConfig.getSULConfig().getSULAdapterConfig();

        return sulAdapterConfigs;
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCstd_SFSstd_SULSstd() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null,
                    new SULClientConfigStandard(null, new SULAdapterConfig() {}), null, null, null),
                () -> new StateFuzzerServerConfigStandard(null,
                    new SULServerConfigStandard(null, new SULAdapterConfig() {}), null, null, null)),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCstd_SFSstd_SULSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null,
                    new SULClientConfigStandard(null, new SULAdapterConfig() {}), null, null, null),
                () -> new StateFuzzerServerConfigStandard(null, new SULServerConfig() {}, null, null, null)),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            null);
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCstd_SFSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null,
                    new SULClientConfigStandard(null, new SULAdapterConfig() {}), null, null, null),
                () -> new StateFuzzerServerConfig() {}),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCemp_SFSstd_SULSstd() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, new SULClientConfig() {}, null, null, null),
                () -> new StateFuzzerServerConfigStandard(null,
                    new SULServerConfigStandard(null, new SULAdapterConfig() {}), null, null, null)),
            null,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd_SULSstd() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfigStandard(null,
                    new SULServerConfigStandard(null, new SULAdapterConfig() {}), null, null, null)),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCemp_SFSstd_SULSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, new SULClientConfig() {}, null, null, null),
                () -> new StateFuzzerServerConfigStandard(null, new SULServerConfig() {}, null, null, null)),
            null,
            null);
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSstd_SULSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfigStandard(null, new SULServerConfig() {}, null, null, null)),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            null);
    }

    @Test
    public void invalidParseWithEmpty_SFCstd_SULCemp_SFSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfigStandard(null, new SULClientConfig() {}, null, null, null),
                () -> new StateFuzzerServerConfig() {}),
            null,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    @Test
    public void invalidParseWithEmpty_SFCemp_SFSemp() {
        invalidParseWithEmpty(
            ConfigBuilders.stateFuzzer(
                () -> new StateFuzzerClientConfig() {},
                () -> new StateFuzzerServerConfig() {}),
            SUL_CLIENT_CONFIG_STANDARD_REQ_ARGS,
            SUL_SERVER_CONFIG_STANDARD_REQ_ARGS);
    }

    private void invalidParseWithEmpty(StateFuzzerConfigBuilder stateFuzzerConfigBuilder, String[] clientReqArgs,
        String[] serverReqArgs) {
        CommandLineParser<?> commandLineParser = CliTestSupport.parser(stateFuzzerConfigBuilder);

        String[] partialArgs = new String[] {"-adapterPort", "adapterPortValue"};

        clientReqArgs = clientReqArgs == null ? new String[0] : clientReqArgs;
        String[] clientPartialArgs = CliTestSupport.concatArgs(partialArgs, clientReqArgs);
        CliTestSupport.assertInvalidClientParse(commandLineParser, clientPartialArgs);

        serverReqArgs = serverReqArgs == null ? new String[0] : serverReqArgs;
        String[] serverPartialArgs = CliTestSupport.concatArgs(partialArgs, serverReqArgs);
        CliTestSupport.assertInvalidServerParse(commandLineParser, serverPartialArgs);
    }
}
