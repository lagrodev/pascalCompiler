package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.interpreter.AstVisitor;

public class UnaryOpNode extends ExpressionNode {
    public final String operator; // "-" или "not"
    public final ExpressionNode operand;

    public UnaryOpNode(String operator, ExpressionNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnaryOp(this);
    }
}
