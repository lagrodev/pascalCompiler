package pascal.custom;

public enum TokenType {
    // ── Ключевые слова — управление ──────────────────────────────
    PROGRAM, VAR, BEGIN, END,
    IF, THEN, ELSE,
    WHILE, DO,
    REPEAT, UNTIL,
    FOR, TO, DOWNTO,
    BREAK, CONTINUE,
    FUNCTION, PROCEDURE,

    // ── Ключевые слова — типы ────────────────────────────────────
    INTEGER, BOOLEAN, STRING_TYPE, ARRAY, OF,

    // ── Ключевые слова — операции ────────────────────────────────
    DIV, MOD, AND, OR, NOT,

    // ── Ключевые слова — ввод/вывод ──────────────────────────────
    WRITE, WRITELN, READ, READLN,

    // ── Литералы ─────────────────────────────────────────────────
    NUMBER, STRING_LITERAL, TRUE, FALSE,

    // ── Идентификатор ────────────────────────────────────────────
    ID,

    // ── Операторы ────────────────────────────────────────────────
    ASSIGN,       // :=
    PLUS,         // +
    MINUS,        // -
    MUL,          // *
    SLASH,        // /  (вещественное деление, пока не используем)

    // Сравнение
    EQUAL,        // =
    NOT_EQUAL,    // <>
    GREATER,      // >
    LESS,         // <
    GREATER_EQ,   // >=
    LESS_EQ,      // <=

    // Пунктуация
    LPAREN,       // (
    RPAREN,       // )
    LBRACKET,     // [
    RBRACKET,     // ]
    COMMA,        // ,
    COLON,        // :
    SEMICOLON,    // ;
    DOT,          // .
    DOT_DOT,      // ..

    // ── Конец файла ──────────────────────────────────────────────
    EOF
}
