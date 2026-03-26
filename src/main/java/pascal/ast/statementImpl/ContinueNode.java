package pascal.ast.statementImpl;

import pascal.ast.StatementNode;
import pascal.interpreter.AstVisitor;

public class ContinueNode extends StatementNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitContinue(this);
    }
}
