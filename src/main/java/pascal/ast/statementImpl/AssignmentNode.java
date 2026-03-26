package pascal.ast.statementImpl;

import pascal.ast.AstNode;
import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

public class AssignmentNode extends StatementNode {
    public final AstNode variable; // VariableNode или ArrayAccessNode
    public final ExpressionNode expression;

    public AssignmentNode(AstNode variable, ExpressionNode expression) {
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignment(this);
    }
}