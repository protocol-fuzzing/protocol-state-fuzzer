package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

import de.learnlib.ralib.automata.Assignment;
import de.learnlib.ralib.automata.MutableRegisterAutomaton;
import de.learnlib.ralib.automata.RALocation;
import de.learnlib.ralib.automata.RegisterAutomaton;
import de.learnlib.ralib.automata.Transition;
import de.learnlib.ralib.automata.output.OutputMapping;
import de.learnlib.ralib.automata.output.OutputTransition;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.SymbolicDataValue.Parameter;
import de.learnlib.ralib.data.SymbolicDataValue.Register;
import de.learnlib.ralib.data.VarMapping;
import de.learnlib.ralib.data.util.SymbolicDataValueGenerator.ParameterGenerator;
import de.learnlib.ralib.data.util.SymbolicDataValueGenerator.RegisterGenerator;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.NumericBooleanExpression;
import gov.nasa.jpf.constraints.expressions.NumericComparator;
import gov.nasa.jpf.constraints.util.ExpressionUtil;

/*
 * --- MSG 0 -->
 * <-- ACK 10 ---
 * --- MSG 10 -->
 * <-- ACK 20 ---
 * --- MSG 20 -->
 * ACK generates the message id that is in the next MSG.
 */

public class ParameterizedServerRA {
    public static final DataType MSG_ID = new DataType("msg_id");

    public static final InputSymbol I_MSG = new InputSymbol("IMSG", new DataType[] {MSG_ID});
    public static final OutputSymbol O_ACK = new OutputSymbol("OACK", new DataType[] {MSG_ID});
    public static final OutputSymbol O_TIMEOUT = new OutputSymbol("OTIMEOUT");

    public static final RegisterAutomaton AUTOMATON = buildParameterizedServerWithFreshOutputValues();

    private static RegisterAutomaton buildParameterizedServerWithFreshOutputValues() {
        MutableRegisterAutomaton ra = new MutableRegisterAutomaton();
        RALocation l0 = ra.addInitialState(true);
        RALocation l1 = ra.addState(true);
        RALocation l2 = ra.addState(true);
        RALocation l3 = ra.addState(true);

        // Symbolic data values
        ParameterGenerator pgen = new ParameterGenerator();
        Parameter pVal = pgen.next(MSG_ID);
        RegisterGenerator rgen = new RegisterGenerator();
        Register rVal = rgen.next(MSG_ID);

        // Guards which appear in the RA
        Expression<Boolean> eqGuard = new NumericBooleanExpression(pVal, NumericComparator.EQ, rVal);
        Expression<Boolean> neqGuard = new NumericBooleanExpression(pVal, NumericComparator.NE, rVal);
        Expression<Boolean> trueGuard = ExpressionUtil.TRUE;

        // Assignments in RA
        Assignment emptyAssignment = new Assignment(new VarMapping<>());
        Assignment storingAssignment = new Assignment(VarMapping.fromPair(rVal, pVal));
        Assignment preservingAssignment = new Assignment(VarMapping.fromPair(rVal, rVal));

        // Mapping for output parameters
        // output mapping for outputs without parameters
        OutputMapping outputMapping = new OutputMapping();
        // output mapping for outputs with a single fresh value.
        OutputMapping freshMapping = new OutputMapping(new ParameterGenerator().next(MSG_ID));

        ra.addTransition(l0, I_MSG, new Transition(I_MSG, trueGuard, l0, l1, emptyAssignment));
        ra.addTransition(l1, O_ACK, new OutputTransition(freshMapping, O_ACK, l1, l2, storingAssignment));
        ra.addTransition(l2, I_MSG, new Transition(I_MSG, eqGuard, l2, l1, emptyAssignment));
        ra.addTransition(l2, I_MSG, new Transition(I_MSG, neqGuard, l2, l3, preservingAssignment));
        ra.addTransition(l3, O_TIMEOUT, new OutputTransition(outputMapping, O_TIMEOUT, l3, l2, preservingAssignment));

        return ra;
    }
}
