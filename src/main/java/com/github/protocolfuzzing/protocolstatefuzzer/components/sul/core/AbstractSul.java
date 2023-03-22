package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core;

import de.learnlib.api.SUL;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers.DynamicPortProvider;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;

public abstract class AbstractSul implements SUL<AbstractInput, AbstractOutput> {
    protected SulConfig sulConfig;
    protected CleanupTasks cleanupTasks;
    protected DynamicPortProvider dynamicPortProvider;
    protected Mapper mapper;

    public AbstractSul(SulConfig sulConfig, CleanupTasks cleanupTasks) {
        this.sulConfig = sulConfig;
        this.cleanupTasks = cleanupTasks;
        // mapper will be provided in subclasses
        this.mapper = null;
    }

    public SulConfig getSulConfig() {
        return sulConfig;
    }

    public CleanupTasks getCleanupTasks() {
        return cleanupTasks;
    }

    public void setDynamicPortProvider(DynamicPortProvider dynamicPortProvider) {
        this.dynamicPortProvider = dynamicPortProvider;
    }

    public Mapper getMapper() {
        return mapper;
    }
}
