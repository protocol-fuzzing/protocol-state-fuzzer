package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.ProcessLaunchTrigger;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.io.PrintWriter;

/**
 * The empty SUL configuration without any JCommander Parameters.
 */
public abstract class SulConfigEmpty implements SulConfig {

    /**
     * Stores the time (ms) the SUL spends waiting for a response.
     * <p>
     * Default value: 100L.
     */
    protected Long responseWait = 100L;

    /**
     * Stores the time (ms) waited after executing the command to start the SUL process.
     * <p>
     * Default value: 0L.
     */
    protected Long startWait = 0L;

    /**
     * Stores the configuration of the Mapper.
     */
    @ParametersDelegate
    protected MapperConfig mapperConfig;

    /**
     * Stores the configuration of the SulAdapter.
     */
    @ParametersDelegate
    protected SulAdapterConfig sulAdapterConfig;

    /**
     * Constructs a new instance by initializing the {@link #mapperConfig} to the empty
     * MapperConfig and the {@link SulAdapterConfig} to the empty SulAdapterConfig.
     */
    public SulConfigEmpty() {
        this.mapperConfig = new MapperConfig(){};
        this.sulAdapterConfig = new SulAdapterConfig(){};
    }

    /**
     * Constructs a new instance from the given parameters.
     * <p>
     * If any given parameter is null then the empty corresponding configuration is used.
     *
     * @param mapperConfig      the configuration of the Mapper
     * @param sulAdapterConfig  the configuration of the SulAdapter
     */
    public SulConfigEmpty(MapperConfig mapperConfig, SulAdapterConfig sulAdapterConfig) {
        this.mapperConfig = mapperConfig == null ? new MapperConfig(){} : mapperConfig;
        this.sulAdapterConfig = sulAdapterConfig == null ? new SulAdapterConfig(){} : sulAdapterConfig;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return  the stored value of {@link #mapperConfig}
     */
    @Override
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #sulAdapterConfig}.
     *
     * @return  the stored value of {@link #sulAdapterConfig}
     */
    @Override
    public SulAdapterConfig getSulAdapterConfig() {
        return sulAdapterConfig;
    }

    /**
     * Returns the stored value of {@link #responseWait}.
     *
     * @return  the stored value of {@link #responseWait}
     */
    @Override
    public Long getResponseWait() {
        return responseWait;
    }

    /**
     * Sets the value of {@link #responseWait}.
     *
     * @param responseWait  the response wait value to be set
     */
    @Override
    public void setResponseWait(Long responseWait) {
        this.responseWait = responseWait;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public InputResponseTimeoutMap getInputResponseTimeout() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getCommand() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getTerminateCommand() {
        return null;
    }

    /**
     * Returns {@code null}.
     *
     * @return  {@code null}
     */
    @Override
    public String getProcessDir() {
        return null;
    }

    /**
     * Returns {@code false}.
     *
     * @return  {@code false}
     */
    @Override
    public boolean isRedirectOutputStreams() {
        return false;
    }

    /**
     * Returns {@link ProcessLaunchTrigger#NEW_TEST}.
     *
     * @return  {@link ProcessLaunchTrigger#NEW_TEST}
     */
    @Override
    public ProcessLaunchTrigger getProcessTrigger() {
        return ProcessLaunchTrigger.NEW_TEST;
    }

    /**
     * Returns the stored value of {@link #startWait}.
     *
     * @return  the stored value of {@link #startWait}
     */
    @Override
    public Long getStartWait() {
        return startWait;
    }

    /**
     * Sets the value of {@link #startWait}.
     *
     * @param startWait  the start wait value to be set
     */
    @Override
    public void setStartWait(Long startWait) {
        this.startWait = startWait;
    }

    @Override
    public void printRunDescriptionSelf(PrintWriter printWriter) {
        printWriter.println("SulConfigEmpty Non-Explicit Parameters");
        printWriter.println("No parameters");
    }

    @Override
    public void printRunDescriptionRec(PrintWriter printWriter) {
        getMapperConfig().printRunDescription(printWriter);
        getSulAdapterConfig().printRunDescription(printWriter);
    }
}
