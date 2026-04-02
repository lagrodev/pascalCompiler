package pascal.util;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
import pascal.interpreter.AstVisitor;

import java.util.ArrayList;
import java.util.List;

public class TreePrinter implements AstVisitor<Void> {
    private String prefix = "";
    private boolean isLast = true;
    private boolean isRoot = true;

    public void print(AstNode node) {
        isRoot = true;
        prefix = "";
        isLast = true;
        node.accept(this);
    }

    private void printLine(String label) {
        if (isRoot) {
            System.out.println(label);
        } else {
            System.out.println(prefix + (isLast ? "└── " : "├── ") + label);
        }
    }

    private void visitChildren(List<? extends AstNode> children) {
        if (children.isEmpty()) return;

        String savedPrefix = prefix;
        boolean savedIsLast = isLast;
        boolean savedIsRoot = isRoot;

        prefix = savedPrefix + (savedIsRoot || savedIsLast ? "    " : "│   ");
        isRoot = false;

        for (int i = 0; i < children.size(); i++) {
            isLast = (i == children.size() - 1);
            children.get(i).accept(this);
        }

        prefix = savedPrefix;
        isLast = savedIsLast;
        isRoot = savedIsRoot;
    }

    @Override
    public Void visitProgram(ProgramNode node) {
        printLine("Program: " + node.name());
        visitChildren(List.of(node.block()));
        return null;
    }

    @Override
    public Void visitBlock(BlockNode node) {
        printLine("Block");
        List<AstNode> children = new ArrayList<>(node.declarations());
        children.add(node.compoundStatement());
        visitChildren(children);
        return null;
    }

    @Override
    public Void visitCompoundStatement(CompoundStatementNode node) {
        printLine("CompoundStatement");
        visitChildren(new ArrayList<>(node.statements));
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode node) {
        printLine("Assignment");
        visitChildren(List.of(node.variable, node.expression));
        return null;
    }

    @Override
    public Void visitVariable(VariableNode node) {
        printLine("Variable (" + node.name + ")");
        return null;
    }

    @Override
    public Void visitLiteral(LiteralNode node) {
        printLine("Literal (" + node.type + ": " + node.value + ")");
        return null;
    }

    @Override
    public Void visitBinaryOp(BinaryOpNode node) {
        printLine("BinaryOp (" + node.operator + ")");
        visitChildren(List.of(node.left, node.right));
        return null;
    }

    @Override
    public Void visitType(TypeNode node) {
        printLine("Type (" + node.typeName + ")");
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclarationNode node) {
        printLine("VarDecl (" + String.join(", ", node.variableNames()) + ")");
        return null;
    }

    @Override
    public Void visitIf(IfNode node) {
        printLine("If");
        List<AstNode> children = new ArrayList<>();
        children.add(node.condition);
        children.add(node.thenBranch);
        if (node.elseBranch != null) children.add(node.elseBranch);
        visitChildren(children);
        return null;
    }

    @Override
    public Void visitWhile(WhileNode node) {
        printLine("While");
        visitChildren(List.of(node.condition, node.body));
        return null;
    }

    @Override
    public Void visitDoWhile(DoWhileNode node) {
        printLine("DoWhile");
        List<AstNode> children = new ArrayList<>(node.body);
        children.add(node.condition);
        visitChildren(children);
        return null;
    }

    @Override
    public Void visitFor(ForNode node) {
        printLine("For (" + node.variableName + ", " + (node.isTo ? "to" : "downto") + ")");
        visitChildren(List.of(node.startValue, node.endValue, node.body));
        return null;
    }

    @Override
    public Void visitProcedureCall(ProcedureCallNode node) {
        printLine("ProcedureCall (" + node.procedureName + ")");
        visitChildren(new ArrayList<>(node.arguments));
        return null;
    }

    @Override
    public Void visitWrite(WriteNode node) {
        printLine(node.newLine ? "Writeln" : "Write");
        visitChildren(new ArrayList<>(node.expressions));
        return null;
    }

    @Override
    public Void visitRead(ReadNode node) {
        printLine("Read");
        visitChildren(new ArrayList<>(node.variables));
        return null;
    }

    @Override
    public Void visitUnaryOp(UnaryOpNode node) {
        printLine("UnaryOp (" + node.operator + ")");
        visitChildren(List.of(node.operand));
        return null;
    }

    @Override
    public Void visitArrayAccess(ArrayAccessNode node) {
        printLine("ArrayAccess (" + node.name + ")");
        visitChildren(List.of(node.index));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode node) {
        printLine("FunctionCall (" + node.functionName + ")");
        visitChildren(new ArrayList<>(node.arguments));
        return null;
    }

    @Override
    public Void visitBreak(BreakNode node) {
        printLine("Break");
        return null;
    }

    @Override
    public Void visitContinue(ContinueNode node) {
        printLine("Continue");
        return null;
    }
}
