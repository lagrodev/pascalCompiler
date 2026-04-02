package pascal.symboltable;

/**
 * Символ переменной. Хранит имя, тип и (если массив) границы.
 */
public class VariableSymbol extends Symbol {
    // Для массивов
    public final boolean isArray;
    public final Integer arrayStart;
    public final Integer arrayEnd;

    /** Обычная переменная */
    public VariableSymbol(String name, String type) {
        super(name, type);
        this.isArray = false;
        this.arrayStart = null;
        this.arrayEnd = null;
    }

    /** Массив */
    public VariableSymbol(String name, String elementType, int start, int end) {
        super(name, "array[" + start + ".." + end + "] of " + elementType);
        this.isArray = true;
        this.arrayStart = start;
        this.arrayEnd = end;
    }

    public String elementType() {
        if (!isArray) return type;
        // "array[1..10] of integer" → "integer"
        int idx = type.lastIndexOf("of ");
        return idx >= 0 ? type.substring(idx + 3).trim() : type;
    }
}
