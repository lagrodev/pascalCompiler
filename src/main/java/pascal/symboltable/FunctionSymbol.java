package pascal.symboltable;

import java.util.List;

/**
 * Символ функции. Хранит параметры и тип возвращаемого значения.
 */
public class FunctionSymbol extends Symbol {
    public final List<VariableSymbol> params;
    public final String returnType;

    public FunctionSymbol(String name, List<VariableSymbol> params, String returnType) {
        super(name, "function:" + returnType);
        this.params = params;
        this.returnType = returnType;
    }
}
