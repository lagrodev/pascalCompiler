package pascal.ast.declarationImpl;

import pascal.ast.AstNode;
import pascal.ast.DeclarationNode;
import pascal.ast.TypeNode;
import pascal.interpreter.AstVisitor;

import java.util.List;

public record VarDeclarationNode(
        List<String> variableNames,
        TypeNode type
) implements AstNode {

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitVarDeclaration(this);
    }
}
