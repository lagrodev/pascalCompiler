package pascal.ast.statementImpl;

import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

import java.util.List;

public class WriteNode extends StatementNode {
    public final List<ExpressionNode> expressions;
    public final boolean newLine; // true = writeln

    public WriteNode(List<ExpressionNode> expressions) {
        this(expressions, false);
    }

    public WriteNode(List<ExpressionNode> expressions, boolean newLine) {
        this.expressions = expressions;
        this.newLine = newLine;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitWrite(this);
    }
}
