package pascal.symboltable;

import pascal.error.SemanticError;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Таблица символов с поддержкой вложенных областей видимости.
 * Каждая область (область видимости) может иметь родителя —
 * поиск символа сначала идёт в текущей области, затем вверх по цепочке.
 */
public class ScopedSymbolTable {
    private final String scopeName;
    private final int scopeLevel;
    private final ScopedSymbolTable parent;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public ScopedSymbolTable(String scopeName, int scopeLevel, ScopedSymbolTable parent) {
        this.scopeName = scopeName;
        this.scopeLevel = scopeLevel;
        this.parent = parent;
        initBuiltins();
    }

    /** Встроенные типы и процедуры Pascal */
    private void initBuiltins() {
        if (scopeLevel == 1) {
            // Встроенные процедуры — используем ProcedureSymbol-заглушки
            symbols.put("write",   new ProcedureSymbol("write",   java.util.List.of()));
            symbols.put("writeln", new ProcedureSymbol("writeln", java.util.List.of()));
            symbols.put("read",    new ProcedureSymbol("read",    java.util.List.of()));
            symbols.put("readln",  new ProcedureSymbol("readln",  java.util.List.of()));
        }
    }

    /** Зарегистрировать символ в текущей области видимости */
    public void define(Symbol symbol) {
        if (symbols.containsKey(symbol.name.toLowerCase())) {
            throw new SemanticError("Переменная '" + symbol.name + "' уже объявлена в данной области видимости");
        }
        symbols.put(symbol.name.toLowerCase(), symbol);
    }

    /**
     * Найти символ: сначала в текущей области, затем в родительских.
     * @return Optional с символом или empty, если не найден
     */
    public Optional<Symbol> lookup(String name) {
        String key = name.toLowerCase();
        if (symbols.containsKey(key)) return Optional.of(symbols.get(key));
        if (parent != null) return parent.lookup(name);
        return Optional.empty();
    }

    /**
     * Найти символ только в текущей (локальной) области видимости.
     */
    public Optional<Symbol> lookupLocal(String name) {
        return Optional.ofNullable(symbols.get(name.toLowerCase()));
    }

    public String getScopeName() { return scopeName; }
    public int getScopeLevel() { return scopeLevel; }
    public ScopedSymbolTable getParent() { return parent; }
    public Map<String, Symbol> getSymbols() { return symbols; }

    /** Печать содержимого таблицы (для отладки) */
    public void print() {
        System.out.printf("%n=== Таблица символов: [%s] (уровень %d) ===%n", scopeName, scopeLevel);
        symbols.forEach((k, v) -> System.out.printf("  %-20s → %s%n", k, v));
    }
}
