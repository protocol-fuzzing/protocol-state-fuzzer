package com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.sulwrappers;

/**
 * Specifies the trigger points when a process should be (re)started.
 * <p>
 * In case of a restart, the previous process is terminated.
 */
public enum ProcessLaunchTrigger {
    /** Once at the start, with termination taking place at the end of learning or testing. */
    START,

    /** Before each test, with termination taking place after the test has been executed. */
    NEW_TEST
}
