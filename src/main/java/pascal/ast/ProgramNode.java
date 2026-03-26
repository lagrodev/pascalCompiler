package pascal.ast;

import pascal.interpreter.AstVisitor;

import java.util.List;

public record ProgramNode(String name, BlockNode block) implements AstNode {
    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }

}