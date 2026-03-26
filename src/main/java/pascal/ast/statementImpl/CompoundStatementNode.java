package pascal.ast.statementImpl;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;
import java.util.List;

public class CompoundStatementNode extends StatementNode {
    public final List<StatementNode> statements;

    public CompoundStatementNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCompoundStatement(this);
    }
}