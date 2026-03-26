package pascal.ast.statementImpl;

import pascal.ast.AstNode;
import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;
import java.util.List;

public class ReadNode extends StatementNode {
    // В read можно передавать переменные или элементы массива (AstNode охватывает VariableNode и ArrayAccessNode)
    public final List<AstNode> variables;

    public ReadNode(List<AstNode> variables) {
        this.variables = variables;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitRead(this);
    }
}