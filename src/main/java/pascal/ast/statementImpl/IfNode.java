package pascal.ast.statementImpl;

import pascal.ast.AstNode;
import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;
public class IfNode extends StatementNode {
    public final ExpressionNode condition;
    public final AstNode thenBranch;
    public final AstNode elseBranch; // Может быть null

    public IfNode(ExpressionNode condition, AstNode thenBranch, AstNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIf(this);
    }
}