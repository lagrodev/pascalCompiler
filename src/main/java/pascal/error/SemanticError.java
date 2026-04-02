package pascal.error;

/**
 * Семантическая ошибка (необъявленная переменная, несовместимые типы и т.д.)
 */
public class SemanticError extends CompilerException {
    public SemanticError(String message, int line) {
        super("Семантическая ошибка: " + message, line);
    }
    public SemanticError(String message) {
        super("Семантическая ошибка: " + message);
    }
}
