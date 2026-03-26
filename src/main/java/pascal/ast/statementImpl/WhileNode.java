package pascal.ast.statementImpl;

import pascal.ast.AstNode;
import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

public class WhileNode extends StatementNode {
    public final ExpressionNode condition;
    public final AstNode body;

    public WhileNode(ExpressionNode condition, AstNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWhile(this);
    }
}