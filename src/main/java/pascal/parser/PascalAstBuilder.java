package pascal.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
import pascal.parser.PascalParser.*;

import java.util.ArrayList;
import java.util.List;

public class PascalAstBuilder extends PascalBaseVisitor<AstNode> {

    @Override
    public AstNode visitProgram(ProgramContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        BlockNode block = (BlockNode) visit(ctx.block());
        return new ProgramNode(name, block);
    }

    @Override
    public AstNode visitBlock(BlockContext ctx) {
        List<AstNode> declarations = new ArrayList<>();

        // Собираем переменные
        if (ctx.varDeclarations() != null) {
            for (VarDeclarationContext varCtx : ctx.varDeclarations().varDeclaration()) {
                declarations.add(visit(varCtx));
            }
        }

        // Собираем функции и процедуры (задел на будущее)
        if (ctx.subprogramDeclarations() != null) {
            // Для 1-й аттестации можно пока просто оставить список или добавить узлы подпрограмм
        }

        AstNode compoundStatement = visit(ctx.compoundStatement());
        return new BlockNode(declarations, compoundStatement);
    }

    @Override
    public AstNode visitVarDeclaration(VarDeclarationContext ctx) {
        List<String> names = new ArrayList<>();
        for (TerminalNode idNode : ctx.identList().IDENTIFIER()) {
            names.add(idNode.getText());
        }
        TypeNode type = (TypeNode) visit(ctx.type());
        return new VarDeclarationNode(names, type);
    }

    @Override
    public AstNode visitType(TypeContext ctx) {
        if (ctx.ARRAY() != null) {
            int start = Integer.parseInt(ctx.NUMBER(0).getText());
            int end = Integer.parseInt(ctx.NUMBER(1).getText());
            String typeName = ctx.simpleType().getText();
            return new TypeNode(typeName, start, end);
        } else {
            return new TypeNode(ctx.simpleType().getText());
        }
    }

    @Override
    public AstNode visitCompoundStatement(CompoundStatementContext ctx) {
        List<StatementNode> statements = new ArrayList<>();
        for (StatementContext stmtCtx : ctx.statementList().statement()) {
            AstNode stmt = visit(stmtCtx);
            if (stmt != null) {
                statements.add((StatementNode) stmt);
            }
        }
        return new CompoundStatementNode(statements);
    }

    @Override
    public AstNode visitAssignmentStatement(AssignmentStatementContext ctx) {
        AstNode varNode = visit(ctx.variable());
        ExpressionNode expr = (ExpressionNode) visit(ctx.expression());
        return new AssignmentNode(varNode, expr);
    }

    @Override
    public AstNode visitIfStatement(IfStatementContext ctx) {
        ExpressionNode condition = (ExpressionNode) visit(ctx.expression());
        AstNode thenBranch = visit(ctx.statement(0));
        AstNode elseBranch = ctx.statement().size() > 1 ? visit(ctx.statement(1)) : null;
        return new IfNode(condition, thenBranch, elseBranch);
    }

    @Override
    public AstNode visitWhileStatement(WhileStatementContext ctx) {
        ExpressionNode condition = (ExpressionNode) visit(ctx.expression());
        AstNode body = visit(ctx.statement());
        return new WhileNode(condition, body);
    }

    @Override
    public AstNode visitWriteStatement(WriteStatementContext ctx) {
        List<ExpressionNode> exprs = new ArrayList<>();
        for (ExpressionContext exprCtx : ctx.expression()) {
            exprs.add((ExpressionNode) visit(exprCtx));
        }
        return new WriteNode(exprs);
    }

    @Override
    public AstNode visitVariable(VariableContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        if (ctx.expression() != null) {
            ExpressionNode index = (ExpressionNode) visit(ctx.expression());
            return new ArrayAccessNode(name, index);
        }
        return new VariableNode(name);
    }

    @Override
    public AstNode visitFactor(FactorContext ctx) {
        if (ctx.NUMBER() != null) {
            return new LiteralNode(Integer.parseInt(ctx.NUMBER().getText()), LiteralNode.LiteralType.INTEGER);
        } else if (ctx.BOOLEAN_LITERAL() != null) {
            return new LiteralNode(Boolean.parseBoolean(ctx.BOOLEAN_LITERAL().getText()), LiteralNode.LiteralType.BOOLEAN);
        } else if (ctx.STRING_LITERAL() != null) {
            // Убираем кавычки
            String text = ctx.STRING_LITERAL().getText();
            return new LiteralNode(text.substring(1, text.length() - 1), LiteralNode.LiteralType.STRING);
        } else if (ctx.variable() != null) {
            return visit(ctx.variable());
        } else if (ctx.NOT() != null) {
            return new UnaryOpNode("not", (ExpressionNode) visit(ctx.factor()));
        } else if (ctx.expression() != null) { // Скобки ( expression )
            return visit((ParseTree) ctx.expression());
        }
        return null;
    }

    // Для бинарных операций (expression, simpleExpression, term) логика схожая.
    // Пример для Term (умножение/деление):
    @Override
    public AstNode visitTerm(TermContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.factor(0));
        for (int i = 1; i < ctx.factor().size(); i++) {
            String op = ctx.MUL_OP(i - 1).getText();
            ExpressionNode right = (ExpressionNode) visit(ctx.factor(i));
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    @Override
    public AstNode visitSimpleExpression(SimpleExpressionContext ctx) {
        // Упрощенная логика (нужно учесть унарный плюс/минус в начале)
        int termIndex = 0;
        ExpressionNode left = (ExpressionNode) visit(ctx.term(termIndex++));

        for (int i = 0; i < ctx.ADD_OP().size(); i++) {
            String op = ctx.ADD_OP(i).getText();
            ExpressionNode right = (ExpressionNode) visit(ctx.term(termIndex++));
            left = new BinaryOpNode(left, op, right);
        }
        return left;
    }

    @Override
    public AstNode visitExpression(ExpressionContext ctx) {
        ExpressionNode left = (ExpressionNode) visit(ctx.simpleExpression(0));
        if (ctx.REL_OP() != null) {
            String op = ctx.REL_OP().getText();
            ExpressionNode right = (ExpressionNode) visit(ctx.simpleExpression(1));
            return new BinaryOpNode(left, op, right);
        }
        return left;
    }
}