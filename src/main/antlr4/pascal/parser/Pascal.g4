grammar Pascal;


// --- Правила парсера ---

program: PROGRAM IDENTIFIER ';' block '.' EOF ;

block: varDeclarations? subprogramDeclarations? compoundStatement ;

varDeclarations: VAR (varDeclaration ';')+ ;
varDeclaration: identList ':' type ;

identList: IDENTIFIER (',' IDENTIFIER)* ;

type
    : simpleType
    | ARRAY '[' NUMBER '..' NUMBER ']' OF simpleType
    ;

simpleType
    : 'integer'
    | 'boolean'
    | 'char'
    ;

subprogramDeclarations: (subprogramDeclaration ';')+ ;

subprogramDeclaration
    : procedureDeclaration
    | functionDeclaration
    ;

procedureDeclaration: PROCEDURE IDENTIFIER parameters? ';' block ;
functionDeclaration: FUNCTION IDENTIFIER parameters? ':' simpleType ';' block ;

parameters: '(' parameterList? ')' ;
parameterList: varDeclaration (';' varDeclaration)* ;

compoundStatement: BEGIN statementList END ;

statementList: statement (';' statement)* ;

statement
    : compoundStatement
    | assignmentStatement
    | procedureCallStatement
    | ifStatement
    | whileStatement
    | doWhileStatement
    | forStatement
    | breakStatement
    | continueStatement
    | writeStatement
    | readStatement
    | /* empty */
    ;

assignmentStatement: variable ':=' expression ;

variable
    : IDENTIFIER
    | IDENTIFIER '[' expression ']'
    ;

procedureCallStatement: IDENTIFIER '(' (expression (',' expression)*)? ')' ;

ifStatement: IF expression THEN statement (ELSE statement)? ;

whileStatement: WHILE expression DO statement ;

doWhileStatement: DO statementList UNTIL expression ;

forStatement: FOR IDENTIFIER ':=' expression (TO | DOWNTO) expression DO statement ;

breakStatement: BREAK ;
continueStatement: CONTINUE ;

writeStatement: WRITE '(' expression (',' expression)* ')' ;
readStatement: READ '(' variable (',' variable)* ')' ;

expression
    : simpleExpression (REL_OP simpleExpression)?
    ;

simpleExpression
    : (PLUS | MINUS)? term (ADD_OP term)*
    ;

term
    : factor (MUL_OP factor)*
    ;

factor
    : variable
    | IDENTIFIER '(' (expression (',' expression)*)? ')'  // Вызов функции
    | NUMBER
    | BOOLEAN_LITERAL
    | CHAR_LITERAL
    | STRING_LITERAL
    | '(' expression ')'
    | NOT factor
    ;

// --- Лексические правила (Токены) ---

PROGRAM: 'program' | 'PROGRAM';
VAR: 'var' | 'VAR';
BEGIN: 'begin' | 'BEGIN';
END: 'end' | 'END';
PROCEDURE: 'procedure' | 'PROCEDURE';
FUNCTION: 'function' | 'FUNCTION';
ARRAY: 'array' | 'ARRAY';
OF: 'of' | 'OF';
IF: 'if' | 'IF';
THEN: 'then' | 'THEN';
ELSE: 'else' | 'ELSE';
WHILE: 'while' | 'WHILE';
DO: 'do' | 'DO';
UNTIL: 'until' | 'UNTIL';
FOR: 'for' | 'FOR';
TO: 'to' | 'TO';
DOWNTO: 'downto' | 'DOWNTO';
BREAK: 'break' | 'BREAK';
CONTINUE: 'continue' | 'CONTINUE';
WRITE: 'write' | 'WRITE';
READ: 'read' | 'READ';
NOT: 'not' | 'NOT';

REL_OP: '=' | '<>' | '<' | '<=' | '>' | '>=';
ADD_OP: PLUS | MINUS | 'or' | 'OR';
MUL_OP: '*' | '/' | 'div' | 'DIV' | 'mod' | 'MOD' | 'and' | 'AND';

PLUS: '+';
MINUS: '-';

NUMBER: [0-9]+ ;
BOOLEAN_LITERAL: 'true' | 'TRUE' | 'false' | 'FALSE' ;
CHAR_LITERAL: '\'' . '\'' ;
STRING_LITERAL: '\'' (~['\r\n])*? '\'' ; // Простые строки для write('Hello')

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]* ;

WS: [ \t\r\n]+ -> skip ;
COMMENT: '{' .*? '}' -> skip ;
LINE_COMMENT: '//' ~[\r\n]* -> skip ;