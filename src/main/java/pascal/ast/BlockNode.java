package pascal.ast;

import pascal.ast.declarationImpl.FunctionDeclarationNode;
import pascal.ast.declarationImpl.ProcedureDeclarationNode;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.interpreter.AstVisitor;

import java.util.List;

public record BlockNode(List<AstNode> declarations, AstNode compoundStatement
) implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitBlock(this);
    }

}