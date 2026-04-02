package pascal.custom;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;

import java.util.ArrayList;
import java.util.List;


public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parse() {
        return program();
    }

    // ── Структура программы ──────────────────────────────────────

    private ProgramNode program() {
        consume(TokenType.PROGRAM, "Ожидалось 'program'");
        Token name = consume(TokenType.ID, "Ожидалось имя программы");
        consume(TokenType.SEMICOLON, "Ожидалась ';'");
        BlockNode block = block();
        consume(TokenType.DOT, "Ожидалась '.' в конце программы");
        return new ProgramNode(name.text, block);
    }

    private BlockNode block() {
        List<AstNode> declarations = new ArrayList<>();

        // var-секция
        if (match(TokenType.VAR)) {
            while (check(TokenType.ID)) {
                declarations.add(varDeclaration());
            }
        }

        CompoundStatementNode compound = compoundStatement();
        return new BlockNode(declarations, compound);
    }

    private VarDeclarationNode varDeclaration() {
        List<String> names = new ArrayList<>();
        names.add(consume(TokenType.ID, "Ожидалось имя переменной").text);
        while (match(TokenType.COMMA)) {
            names.add(consume(TokenType.ID, "Ожидалось имя переменной").text);
        }
        consume(TokenType.COLON, "Ожидалось ':'");
        TypeNode type = type();
        consume(TokenType.SEMICOLON, "Ожидалась ';' после объявления переменной");
        return new VarDeclarationNode(names, type);
    }

    private TypeNode type() {
        if (match(TokenType.INTEGER))     return new TypeNode("integer");
        if (match(TokenType.BOOLEAN))     return new TypeNode("boolean");
        if (match(TokenType.STRING_TYPE)) return new TypeNode("string");
        if (match(TokenType.ARRAY)) {
            consume(TokenType.LBRACKET, "Ожидалось '['");
            int start = Integer.parseInt(consume(TokenType.NUMBER, "Ожидалась нижняя граница массива").text);
            consume(TokenType.DOT_DOT, "Ожидалось '..'");
            int end = Integer.parseInt(consume(TokenType.NUMBER, "Ожидалась верхняя граница массива").text);
            consume(TokenType.RBRACKET, "Ожидалось ']'");
            consume(TokenType.OF, "Ожидалось 'of'");
            TypeNode elemType = type();
            return new TypeNode(elemType.typeName, start, end);
        }
        throw error(peek(), "Ожидался тип данных");
    }

    // ── Операторы ────────────────────────────────────────────────

    private CompoundStatementNode compoundStatement() {
        consume(TokenType.BEGIN, "Ожидалось 'begin'");
        List<StatementNode> stmts = new ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            StatementNode s = statement();
            if (s != null) stmts.add(s);
            if (!check(TokenType.END)) {
                consume(TokenType.SEMICOLON, "Ожидалась ';' между операторами");
            }
        }
        consume(TokenType.END, "Ожидалось 'end'");
        return new CompoundStatementNode(stmts);
    }

    private StatementNode statement() {
        // Пустой оператор
        if (check(TokenType.SEMICOLON) || check(TokenType.END)) return null;

        if (check(TokenType.BEGIN))    return compoundStatement();
        if (match(TokenType.IF))       return ifStatement();
        if (match(TokenType.WHILE))    return whileStatement();
        if (match(TokenType.FOR))      return forStatement();
        if (match(TokenType.REPEAT))   return repeatStatement();
        if (match(TokenType.WRITE))    return writeStatement(false);
        if (match(TokenType.WRITELN))  return writeStatement(true);
        if (match(TokenType.READ))     return readStatement(false);
        if (match(TokenType.READLN))   return readStatement(true);
        if (match(TokenType.BREAK))    return new BreakNode();
        if (match(TokenType.CONTINUE)) return new ContinueNode();

        // Присваивание или вызов процедуры
        if (check(TokenType.ID)) {
            Token id = consume(TokenType.ID, "");
            // Вызов процедуры: ID ( args )
            if (match(TokenType.LPAREN)) {
                List<ExpressionNode> args = argumentList();
                consume(TokenType.RPAREN, "Ожидалась ')'");
                return new ProcedureCallNode(id.text, args);
            }
            // Присваивание: ID [ index ] := expr  или  ID := expr
            AstNode lhs;
            if (match(TokenType.LBRACKET)) {
                ExpressionNode index = expression();
                consume(TokenType.RBRACKET, "Ожидалось ']'");
                lhs = new ArrayAccessNode(id.text, index);
            } else {
                lhs = new VariableNode(id.text);
            }
            consume(TokenType.ASSIGN, "Ожидалось ':='");
            ExpressionNode expr = expression();
            return new AssignmentNode(lhs, expr);
        }

        throw error(peek(), "Неизвестный оператор");
    }

    private IfNode ifStatement() {
        ExpressionNode cond = expression();
        consume(TokenType.THEN, "Ожидалось 'then'");
        AstNode then = statement();
        AstNode els  = null;
        if (match(TokenType.ELSE)) els = statement();
        return new IfNode(cond, then, els);
    }

    private WhileNode whileStatement() {
        ExpressionNode cond = expression();
        consume(TokenType.DO, "Ожидалось 'do'");
        AstNode body = statement();
        return new WhileNode(cond, body);
    }

    private ForNode forStatement() {
        Token var = consume(TokenType.ID, "Ожидалось имя переменной цикла");
        consume(TokenType.ASSIGN, "Ожидалось ':='");
        ExpressionNode start = expression();
        boolean isTo = match(TokenType.TO);
        if (!isTo) consume(TokenType.DOWNTO, "Ожидалось 'to' или 'downto'");
        ExpressionNode end = expression();
        consume(TokenType.DO, "Ожидалось 'do'");
        AstNode body = statement();
        return new ForNode(var.text, start, isTo, end, body);
    }

    private DoWhileNode repeatStatement() {
        List<StatementNode> body = new ArrayList<>();
        while (!check(TokenType.UNTIL) && !isAtEnd()) {
            StatementNode s = statement();
            if (s != null) body.add(s);
            if (!check(TokenType.UNTIL)) {
                match(TokenType.SEMICOLON);
            }
        }
        consume(TokenType.UNTIL, "Ожидалось 'until'");
        ExpressionNode cond = expression();
        return new DoWhileNode(body, cond);
    }

    private WriteNode writeStatement(boolean newLine) {
        List<ExpressionNode> args = new ArrayList<>();
        if (match(TokenType.LPAREN)) {
            args.add(expression());
            while (match(TokenType.COMMA)) args.add(expression());
            consume(TokenType.RPAREN, "Ожидалась ')'");
        }
        if (newLine && args.isEmpty()) {
            args.add(new LiteralNode("", LiteralNode.LiteralType.STRING));
        }
        return new WriteNode(args, newLine);
    }

    private ReadNode readStatement(boolean isReadln) {
        List<AstNode> vars = new ArrayList<>();
        if (match(TokenType.LPAREN)) {
            vars.add(readVariable());
            while (match(TokenType.COMMA)) vars.add(readVariable());
            consume(TokenType.RPAREN, "Ожидалась ')'");
        }
        return new ReadNode(vars);
    }

    private AstNode readVariable() {
        Token id = consume(TokenType.ID, "Ожидалась переменная");
        if (match(TokenType.LBRACKET)) {
            ExpressionNode idx = expression();
            consume(TokenType.RBRACKET, "Ожидалось ']'");
            return new ArrayAccessNode(id.text, idx);
        }
        return new VariableNode(id.text);
    }

    // ── Выражения ────────────────────────────────────────────────

    private ExpressionNode expression() {
        ExpressionNode left = simpleExpression();
        if (check(TokenType.EQUAL)    || check(TokenType.NOT_EQUAL) ||
            check(TokenType.GREATER)  || check(TokenType.LESS)      ||
            check(TokenType.GREATER_EQ) || check(TokenType.LESS_EQ)) {
            Token op = advance();
            ExpressionNode right = simpleExpression();
            return new BinaryOpNode(left, op.text, right);
        }
        return left;
    }

    private ExpressionNode simpleExpression() {
        // Унарный плюс/минус
        if (check(TokenType.MINUS)) {
            advance();
            return new UnaryOpNode("-", term());
        }
        if (check(TokenType.PLUS)) {
            advance();
            return term();
        }
        ExpressionNode expr = term();
        while (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.OR)) {
            Token op = advance();
            expr = new BinaryOpNode(expr, op.text, term());
        }
        return expr;
    }

    private ExpressionNode term() {
        ExpressionNode expr = factor();
        while (check(TokenType.MUL) || check(TokenType.DIV) ||
               check(TokenType.SLASH) || check(TokenType.MOD) || check(TokenType.AND)) {
            Token op = advance();
            // Нормализуем '/' и 'div' к единому оператору '/'
            String opText = (op.type == TokenType.SLASH) ? "/" : op.text;
            expr = new BinaryOpNode(expr, opText, factor());
        }
        return expr;
    }

    private ExpressionNode factor() {
        // Числовой литерал
        if (check(TokenType.NUMBER)) {
            return new LiteralNode(Integer.parseInt(advance().text), LiteralNode.LiteralType.INTEGER);
        }
        // Булевы литералы
        if (match(TokenType.TRUE))  return new LiteralNode(true,  LiteralNode.LiteralType.BOOLEAN);
        if (match(TokenType.FALSE)) return new LiteralNode(false, LiteralNode.LiteralType.BOOLEAN);
        // Строковый литерал
        if (check(TokenType.STRING_LITERAL)) {
            return new LiteralNode(advance().text, LiteralNode.LiteralType.STRING);
        }
        // Унарный NOT
        if (match(TokenType.NOT)) {
            return new UnaryOpNode("not", factor());
        }
        // Унарный минус
        if (match(TokenType.MINUS)) {
            return new UnaryOpNode("-", factor());
        }
        // Скобки
        if (match(TokenType.LPAREN)) {
            ExpressionNode expr = expression();
            consume(TokenType.RPAREN, "Ожидалась ')'");
            return expr;
        }
        // Идентификатор: переменная, индексированный массив или вызов функции
        if (check(TokenType.ID)) {
            Token id = advance();
            // Вызов функции: ID(args)
            if (match(TokenType.LPAREN)) {
                List<ExpressionNode> args = argumentList();
                consume(TokenType.RPAREN, "Ожидалась ')'");
                return new FunctionCallNode(id.text, args);
            }
            // Элемент массива: ID[index]
            if (match(TokenType.LBRACKET)) {
                ExpressionNode idx = expression();
                consume(TokenType.RBRACKET, "Ожидалось ']'");
                return new ArrayAccessNode(id.text, idx);
            }
            return new VariableNode(id.text);
        }
        throw error(peek(), "Ожидалось выражение");
    }

    private List<ExpressionNode> argumentList() {
        List<ExpressionNode> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(expression());
            while (match(TokenType.COMMA)) args.add(expression());
        }
        return args;
    }

    // ── Вспомогательные ─────────────────────────────────────────

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) { current++; return true; }
        }
        return false;
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    private Token advance() {
        return tokens.get(current++);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return tokens.get(current++);
        throw error(peek(), message);
    }

    private Token peek() { return tokens.get(current); }

    private boolean isAtEnd() { return peek().type == TokenType.EOF; }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException(
            "[Строка " + token.line + "] Ошибка парсинга: " + message + " (токен: '" + token.text + "')");
    }
}
