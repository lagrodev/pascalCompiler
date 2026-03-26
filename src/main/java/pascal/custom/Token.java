package pascal.custom;

public class Token {
    public final TokenType type;
    public final String text;
    public final int line;

    public Token(TokenType type, String text, int line) {
        this.type = type;
        this.text = text;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', line %d)", type, text, line);
    }
}