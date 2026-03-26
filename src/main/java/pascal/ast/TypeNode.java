package pascal.ast;

import pascal.interpreter.AstVisitor;

public class TypeNode implements AstNode {
    public final String typeName;
    public final boolean isArray;
    // Границы для массива, например [1..10]
    public final Integer arrayStartIndex;
    public final Integer arrayEndIndex;

    public TypeNode(String typeName) {
        this.typeName = typeName;
        this.isArray = false;
        this.arrayStartIndex = null;
        this.arrayEndIndex = null;
    }

    public TypeNode(String typeName, int start, int end) {
        this.typeName = typeName;
        this.isArray = true;
        this.arrayStartIndex = start;
        this.arrayEndIndex = end;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitType(this);
    }
}