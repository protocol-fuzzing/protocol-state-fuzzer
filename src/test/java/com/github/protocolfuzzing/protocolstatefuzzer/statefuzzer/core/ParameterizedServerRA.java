package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import de.learnlib.ralib.automata.Assignment;
import de.learnlib.ralib.automata.MutableRegisterAutomaton;
import de.learnlib.ralib.automata.RALocation;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.Transition;
import de.learnlib.ralib.automata.TransitionGuard;
import de.learnlib.ralib.automata.guards.AtomicGuardExpression;
import de.learnlib.ralib.automata.guards.Relation;
import de.learnlib.ralib.automata.guards.TrueGuardExpression;
import de.learnlib.ralib.automata.output.OutputMapping;
import de.learnlib.ralib.automata.output.OutputTransition;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.SymbolicDataValue.Parameter;
import de.learnlib.ralib.data.SymbolicDataValue.Register;
import de.learnlib.ralib.data.VarMapping;
import de.learnlib.ralib.data.util.SymbolicDataValueGenerator;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;

/*
 * --- MSG 0  -->
 * <-- ACK 10 ---
 * --- MSG 10 -->
 * <-- ACK 20 ---
 * --- MSG 20 -->
 *
 * ACK generates the message id that is in the next MSG.
 */

public class ParameterizedServerRA {
    public static final DataType MSG_ID = new DataType("msg_id", Integer.class);

    public static final InputSymbol I_MSG = new InputSymbol("IMSG", new DataType[] {MSG_ID});
    public static final OutputSymbol O_ACK = new OutputSymbol("OACK", new DataType[] {MSG_ID});
    public static final OutputSymbol O_TIMEOUT = new OutputSymbol("OTIMEOUT");

    public static final RegisterAutomaton AUTOMATON = buildParameterizedServerWithFreshOutputValues();

    private static RegisterAutomaton buildParameterizedServerWithFreshOutputValues () {
        MutableRegisterAutomaton ra = new MutableRegisterAutomaton();
        RALocation l0 = ra.addInitialState(true);
        RALocation l1 = ra.addState(true);
        RALocation l2 = ra.addState(true);
        RALocation l3 = ra.addState(true);

        // Symbolic data values
        Parameter p1 = new SymbolicDataValueGenerator.ParameterGenerator().next(MSG_ID);
        Register r1 = new SymbolicDataValueGenerator.RegisterGenerator().next(MSG_ID);

        // Guards which appear in the RA
        TransitionGuard trueGuard = new TransitionGuard(new TrueGuardExpression());
        TransitionGuard eqGuard = new TransitionGuard(new AtomicGuardExpression<>(p1, Relation.EQUALS, r1));
        TransitionGuard neqGuard = new TransitionGuard(new AtomicGuardExpression<>(p1, Relation.NOT_EQUALS, r1));

        // Assignments in RA
        Assignment emptyAssignment = new Assignment(new VarMapping<>());
        Assignment storingAssignment = new Assignment(new VarMapping<>(r1, p1));
        Assignment preservingAssignment = new Assignment(new VarMapping<>(r1, r1));

        // Mapping for output parameters
        // output mapping for outputs without parameters
        OutputMapping outputMapping = new OutputMapping();
        // output mapping for outputs with a single fresh value.
        OutputMapping freshMapping = new OutputMapping(new SymbolicDataValueGenerator.ParameterGenerator().next(MSG_ID));

        ra.addTransition(l0, I_MSG, new Transition(I_MSG, trueGuard, l0, l1, emptyAssignment));
        ra.addTransition(l1, O_ACK, new OutputTransition(freshMapping, O_ACK, l1, l2, storingAssignment));
        ra.addTransition(l2, I_MSG, new Transition(I_MSG, eqGuard, l2, l1, emptyAssignment));
        ra.addTransition(l2, I_MSG, new Transition(I_MSG, neqGuard, l2, l3, preservingAssignment));
        ra.addTransition(l3, O_TIMEOUT, new OutputTransition(outputMapping, O_TIMEOUT, l3, l2, preservingAssignment));

        return ra;
    }
}
