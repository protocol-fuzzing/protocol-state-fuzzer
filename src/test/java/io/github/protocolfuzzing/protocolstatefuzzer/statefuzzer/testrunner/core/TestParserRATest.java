package io.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.testrunner.core;

import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.data.DataValue;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.PSymbolInstance;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.word.Word;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

public class TestParserRATest {
    private static final DataType T_SEQ = new DataType("seq");
    private static final DataType T_ACK = new DataType("ack");
    private static final InputSymbol I_MSG = new InputSymbol("IMSG", T_SEQ, T_ACK);
    private static final InputSymbol I_CONNECT = new InputSymbol("ICONNECT");
    private static final Alphabet<InputSymbol> ALPHA = new ListAlphabet<InputSymbol>(Arrays.asList(I_CONNECT, I_MSG));

    @Test
    public void testRead() {
        TestParserRA parser = new TestParserRA();
        Word<PSymbolInstance> actualResult = parser.readTest(ALPHA, Arrays.asList("ICONNECT",
            "ICONNECT[]", "IMSG[0, 1]", "IMSG[4,5]"));
        Word<PSymbolInstance> expectedResult = Word.fromSymbols(
            new PSymbolInstance(I_CONNECT),
            new PSymbolInstance(I_CONNECT),
            new PSymbolInstance(I_MSG,
                new DataValue[] {new DataValue(T_SEQ, new BigDecimal(0)), new DataValue(T_ACK, new BigDecimal(1))}),
            new PSymbolInstance(I_MSG,
                new DataValue[] {new DataValue(T_SEQ, new BigDecimal(4)), new DataValue(T_ACK, new BigDecimal(5))}));
        Assert.assertEquals(expectedResult, actualResult);
    }
}
