package pascal.ast.statementImpl;

import pascal.ast.ExpressionNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

import java.util.List;

public class ProcedureCallNode extends StatementNode {
    public final String procedureName;
    public final List<ExpressionNode> arguments;

    public ProcedureCallNode(String procedureName, List<ExpressionNode> arguments) {
        this.procedureName = procedureName;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProcedureCall(this);
    }
}
