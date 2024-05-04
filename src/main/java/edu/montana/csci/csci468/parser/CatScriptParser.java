package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseStatement(){
        Statement stmt = parseForStatement();
        if(stmt != null){
            return stmt;
        }
        stmt = parseIfStatement();
        if (stmt != null){
            return stmt;
        }
        stmt = parsePrintStatement();
        if(stmt != null){
            return stmt;
        }
        stmt = parseVarStatement();
        if (stmt != null){
            return stmt;
        }
        stmt = parseAssignmentOrFunctionCallStatement();
        if (stmt != null){
            return stmt;
        }
        if (currentFunctionDefinition != null){
            stmt = parseReturnStatement();
            if (stmt != null){
                return stmt;
            }
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseReturnStatement() {
        if (tokens.match(RETURN)){
            Token keyword = tokens.consumeToken();
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
            if (tokens.match(RIGHT_BRACE)){
                return returnStatement;
            } else {
                returnStatement.setExpression(parseExpression());
                return returnStatement;
            }
        }
        else {
            return null;
        }
    }

    private Statement parseAssignmentOrFunctionCallStatement() {
        if (tokens.match(IDENTIFIER)){
            Token identifier = tokens.consumeToken();
            if (tokens.match(EQUAL)){
                AssignmentStatement yeet = new AssignmentStatement();
                yeet.setVariableName(identifier.getStringValue());
                tokens.consumeToken();
                yeet.setExpression(parseExpression());
                return yeet;
            }
            else {
                FunctionCallExpression functionCallExpression = (FunctionCallExpression) parseFunctionCallExpression(identifier);
                return new FunctionCallStatement(functionCallExpression);
            }
        }
        return null;
    }

    private Statement parseVarStatement() {
        Token name = null;
        TypeLiteral type = null;
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            tokens.consumeToken();
            if(tokens.match(IDENTIFIER)){
                name = tokens.consumeToken();
                variableStatement.setVariableName(name.getStringValue());
            } else{
                return null;
            }
            if (tokens.match(COLON)){
                tokens.consumeToken();
                type = parseTypeStatement();
                variableStatement.setExplicitType(type.getType());
            }
            require(EQUAL, variableStatement);
            Expression expression = parseExpression();
            variableStatement.setExpression(expression);
            return variableStatement;
        }
        return null;
    }


    private Statement parseIfStatement() {
        if (tokens.match(IF)) {
            Token ifStart = tokens.consumeToken();
            IfStatement ifStatement = new IfStatement();
            require(LEFT_PAREN, ifStatement);
            Expression testExpression = parseExpression();
            ifStatement.setExpression(testExpression);
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            List<Statement> stms = new ArrayList<>();
            while (tokens.hasMoreTokens() && !tokens.match(RIGHT_BRACE)) {
                Statement stmt = parseStatement();
                stms.add(stmt);
            }
            require(RIGHT_BRACE, ifStatement);
            ifStatement.setTrueStatements(stms);
            if(tokens.match(ELSE)){
                tokens.consumeToken();
                if (tokens.match(IF)){
                    Statement iffys = parseIfStatement();
                }else{
                    require(LEFT_BRACE,ifStatement);
                    List <Statement> elses = new ArrayList<>();
                    while(tokens.hasMoreTokens()&&!tokens.match(RIGHT_BRACE)){
                        Statement elsee = parseStatement();
                        elses.add(elsee);
                    }
                    ifStatement.setElseStatements(elses);
                    require(RIGHT_BRACE, ifStatement);
                }
            }
            return ifStatement;
        } else {
            return null;
        }
    }

    private Statement parseProgramStatement() {
        Statement statement = parseFunctionDefinition();
        if (statement!=null){
            return statement;
        }
        statement = parseStatement();
        if (statement != null) {
            return statement;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());

    }

    private Statement parseForStatement(){
        if(tokens.match(FOR)){

            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            Token identifiertok = require(IDENTIFIER, forStatement);
            forStatement.setVariableName(identifiertok.getStringValue());
            require(IN,forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN,forStatement);
            require(LEFT_BRACE,forStatement);
            ArrayList <Statement> stmtlist = new ArrayList<>();
            while (tokens.hasMoreTokens() && !tokens.match(RIGHT_BRACE)){
                stmtlist.add(parseStatement());
            }
            forStatement.setBody(stmtlist);
            forStatement.setEnd(require(RIGHT_BRACE,forStatement));

            return forStatement;
        }else{
            return null;
        }

    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }
    private TypeLiteral parseTypeStatement() {
        TypeLiteral tl = new TypeLiteral();
        if (tokens.match(IDENTIFIER)){
            String ident = tokens.consumeToken().getStringValue();
            if (ident.equals("int")){
                tl.setType(CatscriptType.INT);
                return tl;
            }
            else if(ident.equals("string")){
                tl.setType(CatscriptType.STRING);
                return tl;
            }
            else if(ident.equals("bool")){
                tl.setType(CatscriptType.BOOLEAN);
                return tl;
            }
            else if(ident.equals("object")){
                tl.setType(CatscriptType.OBJECT);
                return tl;
            }
            else if(ident.equals("list")){
                if (tokens.matchAndConsume(LESS)){
                    tl.setType(CatscriptType.getListType(parseTypeStatement().getType()));
                    require(GREATER, tl);
                    return tl;
                }else{
                    tl.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                    return tl;
                }
            }
            else{
                return null;
            }
        }
        else {
            return null;
        }
    }

    private FunctionDefinitionStatement parseFunctionDefinition(){
        if(tokens.match(FUNCTION)) {
            Token name = tokens.consumeToken();
            FunctionDefinitionStatement funcdef = new FunctionDefinitionStatement();
            currentFunctionDefinition = funcdef;

            Token funcName = require(IDENTIFIER, funcdef);

            //parameter list

            require(LEFT_PAREN, funcdef);

            if (!tokens.match(RIGHT_PAREN)){
                do {
                    TypeLiteral cunt = null;
                    Token parameter = require(IDENTIFIER, funcdef);
                    if(tokens.match(COLON)){
                        tokens.consumeToken();
                        cunt = parseTypeStatement();
                    }
                    funcdef.addParameter(parameter.getStringValue(), cunt);
                }while(tokens.matchAndConsume(COMMA));
            }

            require(RIGHT_PAREN, funcdef);
            TypeLiteral literl = null;
            if (tokens.match(COLON)){
                tokens.consumeToken();
                literl = parseTypeStatement();
            }

            funcdef.setType(literl);

            //function body

            List<Statement> stmts = new ArrayList<>();
            require(LEFT_BRACE,funcdef);
            do{
                if (!tokens.match(RIGHT_BRACE)){
                    Statement s = parseStatement();
                    stmts.add(s);
                }
            } while(!tokens.match(RIGHT_BRACE)&& tokens.hasMoreTokens());

            require(RIGHT_BRACE,funcdef);
            funcdef.setName(funcName.getStringValue());
            funcdef.setBody(stmts);

            currentFunctionDefinition = null;
            if(funcdef.getType()==null){
                funcdef.setType(null);
            }
            return funcdef;

        }
        else{
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseEqualityExpression(){
        Expression expression = parseComparisionExpression();
        while (tokens.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisionExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }
    private Expression parseComparisionExpression(){
        Expression expression = parseAdditiveExpression();
        while (tokens.match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }
    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }
    private Expression parseFactorExpression(){
        Expression expression = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(IDENTIFIER)) {
            Token idToken = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)){
                return parseFunctionCallExpression(idToken);
            }else {
                IdentifierExpression identifierExpression = new IdentifierExpression(idToken.getStringValue());
                identifierExpression.setToken(idToken);
                return identifierExpression;
            }
        } else if (tokens.match(STRING)) {
            Token strToken = tokens.consumeToken();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(strToken.getStringValue());
            stringLiteralExpression.setToken(strToken);
            return stringLiteralExpression;
        } else if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;

        }  else if (tokens.match(TRUE)) {
            Token truetoken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(true);
            booleanLiteralExpression.setToken(truetoken);
            return booleanLiteralExpression;
        }
        else if (tokens.match(FALSE)) {
            Token falsetoken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(false);
            booleanLiteralExpression.setToken(falsetoken);
            return booleanLiteralExpression;
        }else if (tokens.match(NULL)) {
            Token truetoken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(truetoken);
            return nullLiteralExpression;
        }else if (tokens.match(LEFT_PAREN)) {
            Token truetoken = tokens.consumeToken();
            Expression expression = parseExpression();
            Token endtoken = require(RIGHT_PAREN,expression);
            ParenthesizedExpression parn =new ParenthesizedExpression(expression);
            parn.setStart(truetoken);
            parn.setEnd(endtoken);
            return parn;
        } else if (tokens.match(LEFT_BRACKET)){
            Token bracket = tokens.consumeToken();
            List<Expression> values = new ArrayList<>();
            if(!tokens.match(RIGHT_BRACKET)){
                do {
                    Expression expression = parseExpression();
                    values.add(expression);
                } while (tokens.matchAndConsume(COMMA)&& tokens.hasMoreTokens());
            }
            ListLiteralExpression ll = new ListLiteralExpression(values);
            ll.setStart(bracket);
            ll.setEnd(require(RIGHT_BRACKET,ll, ErrorType.UNTERMINATED_LIST));
            return ll;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    private Expression parseFunctionCallExpression(Token idToken) {
        tokens.consumeToken();
        List<Expression> args = new ArrayList<>();
        if(!tokens.match(RIGHT_PAREN)){
            do {
                Expression expression = parseExpression();
                args.add(expression);
            } while(tokens.matchAndConsume(COMMA)&&tokens.hasMoreTokens());
        }
        FunctionCallExpression fc = new FunctionCallExpression(idToken.getStringValue(),args);
        fc.setStart(idToken);
        fc.setEnd(require(RIGHT_PAREN,fc,ErrorType.UNTERMINATED_ARG_LIST));
        return fc;
    }


    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
