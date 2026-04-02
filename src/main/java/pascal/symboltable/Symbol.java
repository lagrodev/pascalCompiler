package pascal.symboltable;

/**
 * Базовый класс символа в таблице символов.
 */
public abstract class Symbol {
    public final String name;
    public final String type; // "integer", "boolean", "string", "array:integer[1..10]" и т.д.

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + name + " : " + type + ")";
    }
}
