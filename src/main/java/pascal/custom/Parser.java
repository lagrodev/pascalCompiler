package pascal.custom;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.BinaryOpNode;
import pascal.ast.expressionImpl.LiteralNode;
import pascal.ast.expressionImpl.VariableNode;
import pascal.ast.statementImpl.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Главная точка входа
    public ProgramNode parse() {
        return program();
    }

    // program = PROGRAM ID SEMICOLON block DOT
    private ProgramNode program() {
        consume(TokenType.PROGRAM, "Ожидалось слово 'program'");
        Token nameToken = consume(TokenType.ID, "Ожидалось имя программы");
        consume(TokenType.SEMICOLON, "Ожидалась ';' после имени программы");

        BlockNode blockNode = block();

        consume(TokenType.DOT, "Ожидалась точка в конце программы");
        return new ProgramNode(nameToken.text, blockNode);
    }

    // block = varDeclarations compoundStatement
    private BlockNode block() {
        List<AstNode> declarations = new ArrayList<>();

        if (match(TokenType.VAR)) {
            while (check(TokenType.ID)) {
                declarations.add(varDeclaration());
            }
        }

        CompoundStatementNode compound = compoundStatement();
        return new BlockNode(declarations, compound);
    }

    // varDeclaration = ID (COMMA ID)* COLON type SEMICOLON
    private VarDeclarationNode varDeclaration() {
        List<String> names = new ArrayList<>();
        names.add(consume(TokenType.ID, "Ожидалось имя переменной").text);

        while (match(TokenType.COMMA)) {
            names.add(consume(TokenType.ID, "Ожидалось имя переменной после запятой").text);
        }

        consume(TokenType.COLON, "Ожидалось ':'");
        TypeNode type = type();
        consume(TokenType.SEMICOLON, "Ожидалась ';' после объявления переменной");

        return new VarDeclarationNode(names, type);
    }

    // type = INTEGER | BOOLEAN | STRING_TYPE (пока упрощенно)
    private TypeNode type() {
        if (match(TokenType.INTEGER)) return new TypeNode("integer");
        if (match(TokenType.BOOLEAN)) return new TypeNode("boolean");
        throw error(peek(), "Ожидался тип данных (integer, boolean)");
    }

    // compoundStatement = BEGIN statementList END
    private CompoundStatementNode compoundStatement() {
        consume(TokenType.BEGIN, "Ожидалось 'begin'");
        List<StatementNode> statements = new ArrayList<>();

        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(statement());
            // В Паскале операторы разделяются точкой с запятой
            if (check(TokenType.SEMICOLON)) {
                consume(TokenType.SEMICOLON, "");
            }
        }

        consume(TokenType.END, "Ожидалось 'end'");
        return new CompoundStatementNode(statements);
    }

    // Разбор одного оператора
    private StatementNode statement() {
        if (check(TokenType.ID)) return assignment();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.WRITE)) return writeStatement();
        if (check(TokenType.BEGIN)) return compoundStatement();

        throw error(peek(), "Неизвестный оператор");
    }

    // assignment = ID ASSIGN expression
    private AssignmentNode assignment() {
        Token varToken = consume(TokenType.ID, "Ожидалась переменная");
        VariableNode varNode = new VariableNode(varToken.text);

        consume(TokenType.ASSIGN, "Ожидалось ':='");
        ExpressionNode expr = expression();
        return new AssignmentNode(varNode, expr);
    }

    // ifStatement = IF expression THEN statement (ELSE statement)?
    private IfNode ifStatement() {
        ExpressionNode condition = expression();
        consume(TokenType.THEN, "Ожидалось 'then'");
        AstNode thenBranch = statement();

        AstNode elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new IfNode(condition, thenBranch, elseBranch);
    }

    // whileStatement = WHILE expression DO statement
    private WhileNode whileStatement() {
        ExpressionNode condition = expression();
        consume(TokenType.DO, "Ожидалось 'do'");
        AstNode body = statement();
        return new WhileNode(condition, body);
    }

    // writeStatement = WRITE LPAREN expression RPAREN
    private WriteNode writeStatement() {
        consume(TokenType.LPAREN, "Ожидалась '('");
        List<ExpressionNode> args = new ArrayList<>();
        args.add(expression()); // Упрощенно: пишем пока один аргумент
        consume(TokenType.RPAREN, "Ожидалась ')'");
        return new WriteNode(args);
    }

    // --- БЛОК ВЫРАЖЕНИЙ (Математика и логика) ---

    private ExpressionNode expression() {
        ExpressionNode expr = simpleExpression();

        if (match(TokenType.GREATER, TokenType.LESS, TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            Token operator = previous();
            ExpressionNode right = simpleExpression();
            expr = new BinaryOpNode(expr, operator.text, right);
        }
        return expr;
    }

    private ExpressionNode simpleExpression() {
        ExpressionNode expr = term();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ExpressionNode right = term();
            expr = new BinaryOpNode(expr, operator.text, right);
        }
        return expr;
    }

    private ExpressionNode term() {
        ExpressionNode expr = factor();

        while (match(TokenType.MUL, TokenType.DIV)) {
            Token operator = previous();
            ExpressionNode right = factor();
            expr = new BinaryOpNode(expr, operator.text, right);
        }
        return expr;
    }

    private ExpressionNode factor() {
        if (match(TokenType.NUMBER)) {
            return new LiteralNode(Integer.parseInt(previous().text), LiteralNode.LiteralType.INTEGER);
        }
        if (match(TokenType.ID)) {
            return new VariableNode(previous().text);
        }
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = expression();
            consume(TokenType.RPAREN, "Ожидалась ')' после выражения");
            return expr;
        }
        throw error(peek(), "Ожидалось число, переменная или '('");
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (Движок парсера) ---

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                current++;
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            current++;
            return previous();
        }
        throw error(peek(), message);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException("[Строка " + token.line + "] Ошибка парсинга: " + message + " (токен: " + token.text + ")");
    }
}