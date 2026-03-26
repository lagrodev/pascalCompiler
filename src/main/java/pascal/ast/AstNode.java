package pascal.ast;

import pascal.interpreter.AstVisitor;

public interface AstNode {
    // Каждый узел должен уметь принимать визитора
    <T> T accept(AstVisitor<T> visitor);
}