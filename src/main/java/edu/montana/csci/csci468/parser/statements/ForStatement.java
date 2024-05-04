package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.Label;
import java.util.LinkedList;
import java.util.List;

public class ForStatement extends Statement {
    private Expression expression;
    private String variableName;
    private List<Statement> body;

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setBody(List<Statement> statements) {
        this.body = new LinkedList<>();
        for (Statement statement : statements) {
            this.body.add(addChild(statement));
        }
    }

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        symbolTable.pushScope();
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            expression.validate(symbolTable);
            CatscriptType type = expression.getType();
            if (type instanceof CatscriptType.ListType) {
                symbolTable.registerSymbol(variableName, getComponentType());
            } else {
                addError(ErrorType.INCOMPATIBLE_TYPES, getStart());
                symbolTable.registerSymbol(variableName, CatscriptType.OBJECT);
            }
        }
        for (Statement statement : body) {
            statement.validate(symbolTable);
        }
        symbolTable.popScope();
    }

    private CatscriptType getComponentType() {
        return ((CatscriptType.ListType) expression.getType()).getComponentType();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime)
    {
        List evaluate = (List) expression.evaluate(runtime);
        for (Object item : evaluate) {
            runtime.pushScope();
            runtime.setValue(variableName, item);
            for (Statement statement : body) {
                statement.execute(runtime);
            }
            runtime.popScope();
        }

    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        //Allocate that anonymous slot
        Integer iteratorSlot = code.nextLocalStorageSlot();
        Integer loopVarSlot = code.createLocalStorageSlotFor(variableName);
        Label loopStart = new Label();
        Label endOfLoop = new Label();
        //This will leave a list on top of the operand stack:
        expression.compile(code);

        //Invoke INVOKEINTERFACE java/util/List.iterator ()Ljava/util/Iterator
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/util/List",
                "iterator", "()Ljava/util/Iterator;");

        code.addVarInstruction(Opcodes.ASTORE, iteratorSlot);

        //Add loopStart (to jump back and end of loop)
        code.addLabel(loopStart);
        //ALOAD the iterator slot:
        code.addVarInstruction(Opcodes.ALOAD,iteratorSlot);


        //Invoke INVOKEINTERFACE hasNext
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/util/Iterator",
                "hasNext", "()Z");
        //IFEQ Jump to endOfLoop label (which we will add later in the code)
        code.addJumpInstruction(Opcodes.IFEQ, endOfLoop);

        //ALOAD iterator again
        code.addVarInstruction(Opcodes.ALOAD,iteratorSlot);
        //Call INVOKEINTERFACE next() on it
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/util/Iterator",
                "next", "()Ljava/lang/Object;");

        //Do a checkcast:
        code.addTypeInstruction(Opcodes.CHECKCAST, ByteCodeGenerator.internalNameFor(getComponentType().getJavaType()));
        //Save that into the loopVarSlot (may be a boolean/int or a ref type):
        if (getComponentType() == CatscriptType.INT || getComponentType() == CatscriptType.BOOLEAN) {
            unbox(code, getComponentType());
            code.addVarInstruction(Opcodes.ISTORE,loopVarSlot);
        } else {
            code.addVarInstruction(Opcodes.ASTORE,loopVarSlot);
        }
        //Compile loop body statements
        for (Statement stmt : body) {
            stmt.compile(code);
        }

        //Goto (Unconditional) the start of the loop
        code.addJumpInstruction(Opcodes.GOTO, loopStart);
        //Add the endOfLoop label.
        code.addLabel(endOfLoop);
    }


}
