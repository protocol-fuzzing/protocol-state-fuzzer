package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.mappers;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;

import java.util.List;

/**
 * It is responsible for receiving protocol messages from the SUL and for
 * the concrete-to-abstract function of the Mapper.
 * <p>
 * It performs the following:
 * <ol>
 * <li>Receives the response from the SUL
 * <li>Updates the execution context
 * <li>Converts the response to a corresponding O
 * </ol>
 * <p>
 * It contains everything related to the conversion of a response to an O.
 * Also there are operations such as coalescing outputs into one or splitting an
 * output into its atoms.
 * <p>
 * The contained OutputBuilder is used to create special symbols or create
 * output symbols after coalescing two outputs.
 *
 * @param <O> the type of outputs
 * @param <P> the type of protocol messages
 * @param <E> the type of execution context
 */
public abstract class OutputMapperRA<O, P, E> {

    /** Stores the constructor parameter. */
    protected MapperConfig mapperConfig;

    /** Stores the constructor parameter. */
    protected OutputBuilder<O> outputBuilder;

    /**
     * Constructs a new instance from the given parameter.
     *
     * @param mapperConfig  the configuration of the Mapper
     * @param outputBuilder the builder of the output symbols
     */
    public OutputMapperRA(MapperConfig mapperConfig, OutputBuilder<O> outputBuilder) {
        this.mapperConfig = mapperConfig;
        this.outputBuilder = outputBuilder;
    }

    /**
     * Returns the stored value of {@link #mapperConfig}.
     *
     * @return the stored value of {@link #mapperConfig}
     */
    public MapperConfig getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Returns the stored value of {@link #outputBuilder}.
     *
     * @return the stored value of {@link #outputBuilder}
     */
    public OutputBuilder<O> getOutputBuilder() {
        return outputBuilder;
    }

    /**
     * Receives the response from the SUL and converts it to a corresponding
     * output symbol.
     *
     * @param context the active execution context holding the protocol-specific
     *                state
     * @return the corresponding output symbol
     */
    public abstract O receiveOutput(E context);

    /**
     * Returns the timeout symbol according to {@link #outputBuilder}.
     *
     * @return the timeout symbol according to {@link #outputBuilder}
     */
    public O timeout() {
        return outputBuilder.buildTimeout();
    }

    /**
     * Returns the timeout symbol or the socket closed symbol according to
     * {@link #outputBuilder} respecting the
     * {@link MapperConfig#isSocketClosedAsTimeout()}.
     *
     * @return the timeout symbol or the socket closed symbol
     */
    public O socketClosed() {
        if (mapperConfig.isSocketClosedAsTimeout()) {
            return outputBuilder.buildTimeout();
        }
        return outputBuilder.buildSocketClosed();
    }

    /**
     * Returns the timeout symbol or the disabled symbol according to
     * {@link #outputBuilder} respecting the
     * {@link MapperConfig#isSocketClosedAsTimeout()}.
     *
     * @return the timeout symbol or the disabled symbol
     */
    public O disabled() {
        if (mapperConfig.isDisabledAsTimeout()) {
            return outputBuilder.buildTimeout();
        }
        return outputBuilder.buildDisabled();
    }

    /**
     * Builds the O output from the given parameters.
     *
     * @param name     the name of the output
     * @param messages the messages of the output
     * @return the corresponding O output
     */
    protected abstract O buildOutput(String name, List<P> messages);
}
