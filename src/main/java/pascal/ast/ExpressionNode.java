package pascal.ast;

public abstract class ExpressionNode implements AstNode {
    /** Заполняется семантическим анализатором ("INTEGER", "BOOLEAN", "STRING", …). */
    public String resolvedType = null;
}
