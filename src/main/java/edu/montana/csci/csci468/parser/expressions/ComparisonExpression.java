package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;



import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class ComparisonExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public ComparisonExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isLessThan() {
        return operator.getType().equals(LESS);
    }

    public boolean isLessThanOrEqual() {
        return operator.getType().equals(LESS_EQUAL);
    }

    public boolean isGreaterThanOrEqual() {
        return operator.getType().equals(GREATER_EQUAL);
    }

    public boolean isGreater() {
        return operator.getType().equals(GREATER);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
        if (!leftHandSide.getType().equals(CatscriptType.INT)) {
            leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
        if (!rightHandSide.getType().equals(CatscriptType.INT)) {
            rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object leftValue = getLeftHandSide().evaluate(runtime);
        Object rightValue = getRightHandSide().evaluate(runtime);
        if (!(leftValue instanceof Integer) || !(rightValue instanceof Integer)) {
            // carson
            return false;
        }
        int leftIntValue = (int) leftValue;
        int rightIntValue = (int) rightValue;
        switch (operator.getType()) {
            case LESS:
                return leftIntValue < rightIntValue;
            case LESS_EQUAL:
                return leftIntValue <= rightIntValue;
            case GREATER_EQUAL:
                return leftIntValue >= rightIntValue;
            case GREATER:
                return leftIntValue > rightIntValue;
            default:
                return false; // carson
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        // X < 10
        getLeftHandSide().compile(code);
        getRightHandSide().compile(code);
        Label trueLabel = new Label();

        //Handle all the cases:
        switch (operator.getType()) {
            case LESS:
                code.addJumpInstruction(Opcodes.IF_ICMPLT, trueLabel);
                //code.addJumpInstruction(Opcodes.IF_ICMPLT,trueLabel);
                break;
            case GREATER:
                code.addJumpInstruction(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case LESS_EQUAL:
                code.addJumpInstruction(Opcodes.IF_ICMPLE, trueLabel);
                break;
            case GREATER_EQUAL:
                code.addJumpInstruction(Opcodes.IF_ICMPGE, trueLabel);
                break;
        }
        //Push false onto the stack then jump over the true label.
        code.pushConstantOntoStack(false);
        //Create another label with an unconditional goto:
        Label defaultLabel = new Label();
        code.addJumpInstruction(Opcodes.GOTO, defaultLabel);

        //Else, we place the true label on the operand stack:
        code.addLabel(trueLabel);
        code.pushConstantOntoStack(true);

        code.addLabel(defaultLabel);
    }
}
