package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import de.learnlib.ralib.automata.Assignment;
import de.learnlib.ralib.automata.MutableRegisterAutomaton;
import de.learnlib.ralib.automata.RALocation;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.Transition;
import de.learnlib.ralib.automata.TransitionGuard;
import de.learnlib.ralib.automata.guards.TrueGuardExpression;
import de.learnlib.ralib.automata.output.OutputMapping;
import de.learnlib.ralib.automata.output.OutputTransition;
import de.learnlib.ralib.data.VarMapping;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;

public class BasicServerRA {

    public static final InputSymbol I_CONNECT = new InputSymbol("IConnect");
    public static final InputSymbol I_MSG = new InputSymbol("IMSG");
    public static final OutputSymbol O_ACK = new OutputSymbol("OACK");
    public static final OutputSymbol O_TIMEOUT = new OutputSymbol("OTIMEOUT");

    public static final RegisterAutomaton AUTOMATON = buildBasicServerRA();

    private static RegisterAutomaton buildBasicServerRA() {
        MutableRegisterAutomaton ra = new MutableRegisterAutomaton();
        RALocation l0 = ra.addInitialState(true);
        RALocation l1 = ra.addState(true);
        RALocation l2 = ra.addState(true);
        RALocation l3 = ra.addState(true);
        RALocation l4 = ra.addState(true);
        TransitionGuard trueGuard = new TransitionGuard(new TrueGuardExpression());
        Assignment emptyAssignment = new Assignment(new VarMapping<>());
        OutputMapping outputMapping = new OutputMapping();
        ra.addTransition(l0, I_CONNECT, new Transition(I_CONNECT, trueGuard, l0, l1, emptyAssignment));
        ra.addTransition(l0, I_MSG, new Transition(I_MSG, trueGuard, l0, l3, emptyAssignment));
        ra.addTransition(l1, O_TIMEOUT, new OutputTransition(outputMapping, O_TIMEOUT, l1, l2, emptyAssignment));
        ra.addTransition(l2, I_CONNECT, new Transition(I_CONNECT, trueGuard, l2, l1, emptyAssignment));
        ra.addTransition(l2, I_CONNECT, new Transition(I_MSG, trueGuard, l2, l4, emptyAssignment));
        ra.addTransition(l3, O_TIMEOUT, new OutputTransition(outputMapping, O_TIMEOUT, l3, l0, emptyAssignment));
        ra.addTransition(l4, O_ACK, new OutputTransition(outputMapping, O_ACK, l4, l2, emptyAssignment));
        return ra;
    }
}
