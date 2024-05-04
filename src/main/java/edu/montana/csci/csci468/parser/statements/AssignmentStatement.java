package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class AssignmentStatement extends Statement {
    private Expression expression;
    private String variableName;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    public boolean isGlobal()
    {
        return getParent() instanceof CatScriptProgram;
    }
    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        CatscriptType symbolType = symbolTable.getSymbolType(getVariableName());
        if (symbolType == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            // TOOD - verify compatilibity of types
            if (!expression.getType().equals(symbolTable)){
                addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
    }




    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime)
    {
       runtime.setValue(variableName, expression.evaluate(runtime));
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        //Like Variable statement, without allocating:
        if(isGlobal()){
            //Push the 'this' pointer
            code.addVarInstruction(Opcodes.ALOAD,0);
            //Compile the expression
            expression.compile(code);
            //Save the expression to the field
            code.addFieldInstruction(Opcodes.PUTFIELD, variableName,
                    code.getProgramInternalName(),expression.getType().toString());
        } else {
            if(expression.getType().equals(CatscriptType.INT) || expression.getType().equals( CatscriptType.BOOLEAN)){
                code.addVarInstruction(Opcodes.ILOAD,code.createLocalStorageSlotFor(getVariableName()));
            } else {
                code.addVarInstruction(Opcodes.ASTORE, code.createLocalStorageSlotFor(getVariableName()));
            }
            expression.compile(code);
        }
    }
}
