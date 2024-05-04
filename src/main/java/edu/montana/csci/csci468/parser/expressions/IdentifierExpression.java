package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

public class IdentifierExpression extends Expression {
    private final String name;
    private CatscriptType type;

    public IdentifierExpression(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        return runtime.getValue(name);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        //Some useful functions:
        Integer i = code.resolveLocalStorageSlotFor(name);
        if (i != null){
            //Look up in the given slot
            if(getType().equals(CatscriptType.INT) || getType().equals(CatscriptType.BOOLEAN))
            {
                //Load the value into the slot:
                code.addVarInstruction(Opcodes.ILOAD, i);
            }
            else
            {
                //Load as a field: ALoad
                code.addVarInstruction(Opcodes.ALOAD, i);
            }
        } else {
            //This case should only occur if the variable isn't local. Assume a field:
            //Test the type again:
            code.addVarInstruction(Opcodes.ALOAD, 0);
            if(getType().equals(CatscriptType.INT) ||  getType().equals(CatscriptType.BOOLEAN)){
                //Load the value into the slot:
                code.addFieldInstruction(Opcodes.GETFIELD, name, "I", code.getProgramInternalName());
            }
            else
            {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "L"+ByteCodeGenerator.internalNameFor(getType().getJavaType())+";", code.getProgramInternalName());
            }
            //Add a field instruction:
            //code.addFieldInstruction(Opcodes.GETFIELD, name, code.getProgramInternalName(),
            //"Ljava/lang/Object;");
        }
    }


}
