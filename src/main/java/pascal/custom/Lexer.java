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

            // Числа
            if (Character.isDigit(current)) {
                tokens.add(new Token(TokenType.NUMBER, readNumber(), line));
                continue;
            }

            // Идентификаторы и ключевые слова
            if (Character.isLetter(current) || current == '_') {
                String word = readWord();
                TokenType type = determineKeywordType(word);
                tokens.add(new Token(type, word, line));
                continue;
            }

            // Операторы присваивания и пунктуация
            if (current == ':' && peek() == '=') {
                tokens.add(new Token(TokenType.ASSIGN, ":=", line));
                pos += 2;
                continue;
            }

            // Простые односимвольные токены
            switch (current) {
                case '+': tokens.add(new Token(TokenType.PLUS, "+", line)); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS, "-", line)); pos++; break;
                case '*': tokens.add(new Token(TokenType.MUL, "*", line)); pos++; break;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";", line)); pos++; break;
                case ':': tokens.add(new Token(TokenType.COLON, ":", line)); pos++; break;
                case ',': tokens.add(new Token(TokenType.COMMA, ",", line)); pos++; break;
                case '.': tokens.add(new Token(TokenType.DOT, ".", line)); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN, "(", line)); pos++; break;
                case ')': tokens.add(new Token(TokenType.RPAREN, ")", line)); pos++; break;
                case '=': tokens.add(new Token(TokenType.EQUAL, "=", line)); pos++; break;
                case '>': tokens.add(new Token(TokenType.GREATER, ">", line)); pos++; break;
                case '<': tokens.add(new Token(TokenType.LESS, "<", line)); pos++; break;
                default:
                    throw new RuntimeException("Unknown character: " + current + " at line " + line);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    private String readWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    private char peek() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private TokenType determineKeywordType(String word) {
        switch (word.toLowerCase()) {
            case "program": return TokenType.PROGRAM;
            case "var": return TokenType.VAR;
            case "begin": return TokenType.BEGIN;
            case "end": return TokenType.END;
            case "if": return TokenType.IF;
            case "then": return TokenType.THEN;
            case "else": return TokenType.ELSE;
            case "while": return TokenType.WHILE;
            case "do": return TokenType.DO;
            case "integer": return TokenType.INTEGER;
            case "write": return TokenType.WRITE;
            default: return TokenType.ID; // Если не ключевое слово, значит имя переменной
        }
    }
}