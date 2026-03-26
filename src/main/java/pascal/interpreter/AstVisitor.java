package pascal.interpreter;

import pascal.ast.*;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
public interface AstVisitor<T> {
    // Структура программы
    T visitProgram(ProgramNode node);
    T visitBlock(BlockNode node);
    T visitType(TypeNode node);
    T visitVarDeclaration(VarDeclarationNode node);

    // Операторы (Statements)
    T visitCompoundStatement(CompoundStatementNode node);
    T visitAssignment(AssignmentNode node);
    T visitIf(IfNode node);
    T visitWhile(WhileNode node);
    T visitDoWhile(DoWhileNode node);
    T visitFor(ForNode node);
    T visitProcedureCall(ProcedureCallNode node);
    T visitWrite(WriteNode node);
    T visitRead(ReadNode node);
    T visitBreak(BreakNode node);
    T visitContinue(ContinueNode node);

    // Выражения (Expressions)
    T visitBinaryOp(BinaryOpNode node);
    T visitUnaryOp(UnaryOpNode node);
    T visitLiteral(LiteralNode node);
    T visitVariable(VariableNode node);
    T visitArrayAccess(ArrayAccessNode node);
    T visitFunctionCall(FunctionCallNode node);
}