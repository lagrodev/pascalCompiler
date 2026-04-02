package pascal.error;

public class CompilerException extends RuntimeException {
    private final int line;

    public CompilerException(String message, int line) {
        super("[Строка " + line + "] " + message);
        this.line = line;
    }

    public CompilerException(String message) {
        super(message);
        this.line = -1;
    }

    public int getLine() { return line; }
}
