package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

public class BinaryOpNode extends ExpressionNode {
    public final ExpressionNode left;
    public final String operator;
    public final ExpressionNode right;

    public BinaryOpNode(ExpressionNode left, String operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBinaryOp(this);
    }
}
