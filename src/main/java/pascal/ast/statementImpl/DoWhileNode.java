package pascal.ast.statementImpl;

import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;
import java.util.List;

public class DoWhileNode extends StatementNode {
    public final List<StatementNode> body; // В Pascal между do и until можно писать несколько стейтментов без begin/end
    public final ExpressionNode condition;

    public DoWhileNode(List<StatementNode> body, ExpressionNode condition) {
        this.body = body;
        this.condition = condition;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitDoWhile(this);
    }
}