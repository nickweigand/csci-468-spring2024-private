package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

public class UnaryExpression extends Expression {

    private final Token operator;
    private final Expression rightHandSide;

    public UnaryExpression(Token operator, Expression rightHandSide) {
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    public boolean isMinus() {
        return operator.getType().equals(TokenType.MINUS);
    }

    public boolean isNot() {
        return !isMinus();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        rightHandSide.validate(symbolTable);
        if (isNot() && !rightHandSide.getType().equals(CatscriptType.BOOLEAN)) {
            addError(ErrorType.INCOMPATIBLE_TYPES);
        } else if(isMinus() && !rightHandSide.getType().equals(CatscriptType.INT)) {
            addError(ErrorType.INCOMPATIBLE_TYPES);
        }
    }

    @Override
    public CatscriptType getType() {
        if (isMinus()) {
            return CatscriptType.INT;
        } else {
            return CatscriptType.BOOLEAN;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object rhsValue = getRightHandSide().evaluate(runtime);
        if (this.isMinus()) {
            return -1 * (Integer) rhsValue;
        } else {
            return !(Boolean) rhsValue;
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Label unaryL = new Label();

        getRightHandSide().compile(code);
        if(isMinus()){
            code.addInstruction(Opcodes.INEG);
        } else if(isNot()){
            //Load the constant -1 onto the stack:
            code.pushConstantOntoStack(-1);
            //The use the IXOR Instruction to flip the bits:
            code.addInstruction(Opcodes.IXOR);
            //If the result is true (1), jump to truelabel, and push that value onto the stack:
            code.addJumpInstruction(Opcodes.IFEQ, unaryL);
            //Else (0), push 0 onto the stack
            code.addJumpInstruction(Opcodes.GOTO,unaryL);
            //Lastly, add this label:
            code.addLabel(unaryL);
            //Continue on if false:
            code.pushConstantOntoStack(0);
        }
    }


}

