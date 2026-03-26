package pascal.custom;

public enum TokenType {
    // Ключевые слова
    PROGRAM, VAR, BEGIN, END, IF, THEN, ELSE, WHILE, DO, FOR, TO, DOWNTO, READ, WRITE,
    // Типы данных
    INTEGER, BOOLEAN, STRING_TYPE,
    // Операторы и пунктуация
    ASSIGN,       // :=
    PLUS, MINUS, MUL, DIV, // + - * /
    GREATER, LESS, EQUAL, NOT_EQUAL, // > < = <>
    LPAREN, RPAREN, // ( )
    LBRACKET, RBRACKET, // [ ]
    COMMA, COLON, SEMICOLON, DOT, // , : ; .
    // Литералы и идентификаторы
    ID, NUMBER, STRING_LITERAL,
    EOF // Конец файла
}