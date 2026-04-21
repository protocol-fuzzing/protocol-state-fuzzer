package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.difftest.core;

import java.util.function.BiPredicate;

/**
 * Example class for defining output equivalence.
 */
public class DtlsOutputEquivalence implements BiPredicate<String, String> {

    @Override
    public boolean test(String a, String b) {
        if (a.equals(b))
            return true;

        if (isFatalAlert(a) && isFatalAlert(b))
            return true;

        return false;
    }

    private boolean isFatalAlert(String output) {
        return output.startsWith("Alert(FATAL,");
    }
}
