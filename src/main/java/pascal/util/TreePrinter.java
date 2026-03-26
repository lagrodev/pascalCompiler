package pascal.util;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
import pascal.interpreter.AstVisitor;

public class TreePrinter implements AstVisitor<Void> {
    private int indentLevel = 0;

    private void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            System.out.print("  ");
        }
    }
    public void print(AstNode node) {
        node.accept(this);
    }

    @Override
    public Void visitProgram(ProgramNode node) {
        printIndent(); System.out.println("Program: " + node.name());
        indentLevel++;
        node.block().accept(this);
        indentLevel--;
        return null;
    }

    @Override
    public Void visitBlock(BlockNode node) {
        printIndent(); System.out.println("Block");
        indentLevel++;
        for (AstNode decl : node.declarations()) decl.accept(this);
        node.compoundStatement().accept(this);
        indentLevel--;
        return null;
    }

    @Override
    public Void visitCompoundStatement(CompoundStatementNode node) {
        printIndent(); System.out.println("CompoundStatement");
        indentLevel++;
        for (StatementNode stmt : node.statements) stmt.accept(this);
        indentLevel--;
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode node) {
        printIndent(); System.out.println("Assignment");
        indentLevel++;
        node.variable.accept(this);
        node.expression.accept(this);
        indentLevel--;
        return null;
    }

    @Override
    public Void visitVariable(VariableNode node) {
        printIndent(); System.out.println("Variable (" + node.name + ")");
        return null;
    }

    @Override
    public Void visitLiteral(LiteralNode node) {
        printIndent(); System.out.println("Literal (" + node.type + ": " + node.value + ")");
        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOpNode node) {
        printIndent(); System.out.println("BinaryOp (" + node.operator + ")");
        indentLevel++;
        node.left.accept(this);
        node.right.accept(this);
        indentLevel--;
        return null;
    }

    // Заглушки для остальных узлов, чтобы класс скомпилировался
    @Override public Void visitType(TypeNode node) { printIndent(); System.out.println("Type (" + node.typeName + ")"); return null; }
    @Override public Void visitVarDeclaration(VarDeclarationNode node) { printIndent(); System.out.println("VarDecl (" + String.join(", ", node.variableNames()) + ")"); return null; }
    @Override public Void visitIf(IfNode node) { printIndent(); System.out.println("If"); indentLevel++; node.condition.accept(this); node.thenBranch.accept(this); if (node.elseBranch != null) node.elseBranch.accept(this); indentLevel--; return null; }
    @Override public Void visitWhile(WhileNode node) { printIndent(); System.out.println("While"); indentLevel++; node.condition.accept(this); node.body.accept(this); indentLevel--; return null; }
    @Override public Void visitWrite(WriteNode node) { printIndent(); System.out.println("Write"); return null; }
    @Override public Void visitDoWhile(DoWhileNode node) { return null; }
    @Override public Void visitFor(ForNode node) { return null; }
    @Override public Void visitProcedureCall(ProcedureCallNode node) { return null; }
    @Override public Void visitRead(ReadNode node) { return null; }
    @Override public Void visitBreak(BreakNode node) { return null; }
    @Override public Void visitContinue(ContinueNode node) { return null; }
    @Override public Void visitUnaryOp(UnaryOpNode node) { return null; }
    @Override public Void visitArrayAccess(ArrayAccessNode node) { return null; }
    @Override public Void visitFunctionCall(FunctionCallNode node) { return null; }
}
