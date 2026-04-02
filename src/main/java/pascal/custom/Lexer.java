package pascal.custom;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char current = input.charAt(pos);

            // Пропускаем пробелы и переносы строк
            if (Character.isWhitespace(current)) {
                if (current == '\n') line++;
                pos++;
                continue;
            }

            // Комментарии { ... }
            if (current == '{') {
                while (pos < input.length() && input.charAt(pos) != '}') {
                    if (input.charAt(pos) == '\n') line++;
                    pos++;
                }
                pos++; // пропустить '}'
                continue;
            }

            // Числа
            if (Character.isDigit(current)) {
                tokens.add(new Token(TokenType.NUMBER, readNumber(), line));
                continue;
            }

            // Строковые литералы 'abc'
            if (current == '\'') {
                tokens.add(new Token(TokenType.STRING_LITERAL, readString(), line));
                continue;
            }

            // Идентификаторы и ключевые слова
            if (Character.isLetter(current) || current == '_') {
                String word = readWord();
                TokenType type = determineKeywordType(word);
                tokens.add(new Token(type, word, line));
                continue;
            }

            // Двухсимвольные операторы: := >= <= <>
            if (current == ':' && peek() == '=') {
                tokens.add(new Token(TokenType.ASSIGN, ":=", line)); pos += 2; continue;
            }
            if (current == '>' && peek() == '=') {
                tokens.add(new Token(TokenType.GREATER_EQ, ">=", line)); pos += 2; continue;
            }
            if (current == '<' && peek() == '=') {
                tokens.add(new Token(TokenType.LESS_EQ, "<=", line)); pos += 2; continue;
            }
            if (current == '<' && peek() == '>') {
                tokens.add(new Token(TokenType.NOT_EQUAL, "<>", line)); pos += 2; continue;
            }
            if (current == '.' && peek() == '.') {
                tokens.add(new Token(TokenType.DOT_DOT, "..", line)); pos += 2; continue;
            }

            // Однобуквенные токены
            switch (current) {
                case '+' -> { tokens.add(new Token(TokenType.PLUS,      "+", line)); pos++; }
                case '-' -> { tokens.add(new Token(TokenType.MINUS,     "-", line)); pos++; }
                case '*' -> { tokens.add(new Token(TokenType.MUL,       "*", line)); pos++; }
                case '/' -> { tokens.add(new Token(TokenType.SLASH,     "/", line)); pos++; }
                case ';' -> { tokens.add(new Token(TokenType.SEMICOLON, ";", line)); pos++; }
                case ':' -> { tokens.add(new Token(TokenType.COLON,     ":", line)); pos++; }
                case ',' -> { tokens.add(new Token(TokenType.COMMA,     ",", line)); pos++; }
                case '.' -> { tokens.add(new Token(TokenType.DOT,       ".", line)); pos++; }
                case '(' -> { tokens.add(new Token(TokenType.LPAREN,    "(", line)); pos++; }
                case ')' -> { tokens.add(new Token(TokenType.RPAREN,    ")", line)); pos++; }
                case '[' -> { tokens.add(new Token(TokenType.LBRACKET,  "[", line)); pos++; }
                case ']' -> { tokens.add(new Token(TokenType.RBRACKET,  "]", line)); pos++; }
                case '=' -> { tokens.add(new Token(TokenType.EQUAL,     "=", line)); pos++; }
                case '>' -> { tokens.add(new Token(TokenType.GREATER,   ">", line)); pos++; }
                case '<' -> { tokens.add(new Token(TokenType.LESS,      "<", line)); pos++; }
                default  -> throw new RuntimeException(
                        "Неизвестный символ: '" + current + "' на строке " + line);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos++));
        }
        return sb.toString();
    }

    private String readWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() &&
               (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos++));
        }
        return sb.toString();
    }

    private String readString() {
        pos++; // пропустить открывающую '
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && input.charAt(pos) != '\'') {
            sb.append(input.charAt(pos++));
        }
        pos++; // пропустить закрывающую '
        return sb.toString();
    }

    private char peek() {
        return (pos + 1 < input.length()) ? input.charAt(pos + 1) : '\0';
    }

    private TokenType determineKeywordType(String word) {
        return switch (word.toLowerCase()) {
            case "program"  -> TokenType.PROGRAM;
            case "var"      -> TokenType.VAR;
            case "begin"    -> TokenType.BEGIN;
            case "end"      -> TokenType.END;
            case "if"       -> TokenType.IF;
            case "then"     -> TokenType.THEN;
            case "else"     -> TokenType.ELSE;
            case "while"    -> TokenType.WHILE;
            case "do"       -> TokenType.DO;
            case "for"      -> TokenType.FOR;
            case "to"       -> TokenType.TO;
            case "downto"   -> TokenType.DOWNTO;
            case "repeat"   -> TokenType.REPEAT;
            case "until"    -> TokenType.UNTIL;
            case "break"    -> TokenType.BREAK;
            case "continue" -> TokenType.CONTINUE;
            case "integer"  -> TokenType.INTEGER;
            case "boolean"  -> TokenType.BOOLEAN;
            case "string"   -> TokenType.STRING_TYPE;
            case "array"    -> TokenType.ARRAY;
            case "of"       -> TokenType.OF;
            case "div"      -> TokenType.DIV;
            case "mod"      -> TokenType.MOD;
            case "and"      -> TokenType.AND;
            case "or"       -> TokenType.OR;
            case "not"      -> TokenType.NOT;
            case "true"     -> TokenType.TRUE;
            case "false"    -> TokenType.FALSE;
            case "write"    -> TokenType.WRITE;
            case "writeln"  -> TokenType.WRITELN;
            case "read"     -> TokenType.READ;
            case "readln"   -> TokenType.READLN;
            case "function" -> TokenType.FUNCTION;
            case "procedure"-> TokenType.PROCEDURE;
            default         -> TokenType.ID;
        };
    }
}
