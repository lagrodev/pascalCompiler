package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.interpreter.AstVisitor;

public class ArrayAccessNode extends ExpressionNode {
    public final String name; // Имя массива
    public final ExpressionNode index; // Индекс может быть любым выражением

    public ArrayAccessNode(String name, ExpressionNode index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }
}