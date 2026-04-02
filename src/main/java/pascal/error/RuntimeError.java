package pascal.error;

/**
 * Ошибка времени выполнения в VM (деление на 0, выход за границы массива и т.д.)
 */
public class RuntimeError extends CompilerException {
    public RuntimeError(String message) {
        super("Ошибка времени выполнения: " + message);
    }
}
