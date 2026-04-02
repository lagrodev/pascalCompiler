package pascal.symboltable;

import java.util.List;

/**
 * Символ процедуры. Хранит имена и типы параметров.
 */
public class ProcedureSymbol extends Symbol {
    public final List<VariableSymbol> params;

    public ProcedureSymbol(String name, List<VariableSymbol> params) {
        super(name, "procedure");
        this.params = params;
    }
}
