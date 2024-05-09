package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.* ;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static edu.montana.csci.csci468.tokenizer.TokenType.GREATER;
import static org.junit.jupiter.api.Assertions.*;

public class PartnerTest extends CatscriptTestBase {


    void divisionByZeroThrowsError()
    {
        assertThrows(ArithmeticException.class, () -> executeProgram("var x = 1 / 0"));
    }


    public void parseListLitExpr()
    {
        FunctionCallExpression expr = parseExpression("plug([3,5,6] , [2,4,9])", false);
        assertEquals("plug", expr.getName());
        assertEquals(2, expr.getArguments().size());
        LinkedList<Expression> list = (LinkedList<Expression>) expr.getArguments();
        assertTrue(list.get(0) instanceof ListLiteralExpression);
        assertTrue(list.get(1) instanceof ListLiteralExpression);
    }
    @Test
    public void parseEqualityNestUnary()
    {
        EqualityExpression expr = parseExpression("not false != not not true");
        assertEquals(false, expr.isEqual());
        assertTrue(expr.getRightHandSide() instanceof UnaryExpression);
        assertTrue(expr.getLeftHandSide() instanceof UnaryExpression);
    }

}
