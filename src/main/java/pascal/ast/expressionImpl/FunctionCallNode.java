package pascal.ast.expressionImpl;

import pascal.ast.ExpressionNode;
import pascal.interpreter.AstVisitor;
import java.util.List;

public class FunctionCallNode extends ExpressionNode {
    public final String functionName;
    public final List<ExpressionNode> arguments;

    public FunctionCallNode(String functionName, List<ExpressionNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }
}