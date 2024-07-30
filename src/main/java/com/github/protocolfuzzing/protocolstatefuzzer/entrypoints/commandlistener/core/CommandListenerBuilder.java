package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core;

import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core.config.CommandListenerEnabler;

public interface CommandListenerBuilder {
    CommandListener build(CommandListenerEnabler enabler);
}
