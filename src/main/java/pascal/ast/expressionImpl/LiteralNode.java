package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.interpreter.AstVisitor;

public class LiteralNode extends ExpressionNode {
    public final Object value;
    public final LiteralType type;

    public enum LiteralType { INTEGER, BOOLEAN, CHAR, STRING }

    public LiteralNode(Object value, LiteralType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}
