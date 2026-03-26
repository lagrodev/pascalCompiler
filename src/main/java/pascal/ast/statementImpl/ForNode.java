package pascal.ast.statementImpl;

import pascal.ast.AstNode;
import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

public class ForNode extends StatementNode {
    public final String variableName;
    public final ExpressionNode startValue;
    public final boolean isTo; // true если 'to', false если 'downto'
    public final ExpressionNode endValue;
    public final AstNode body;

    public ForNode(String variableName, ExpressionNode startValue, boolean isTo, ExpressionNode endValue, AstNode body) {
        this.variableName = variableName;
        this.startValue = startValue;
        this.isTo = isTo;
        this.endValue = endValue;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFor(this);
    }
}
