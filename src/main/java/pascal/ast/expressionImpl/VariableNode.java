package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.interpreter.AstVisitor;

public class VariableNode extends ExpressionNode {
    public final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitVariable(this);
    }
}
