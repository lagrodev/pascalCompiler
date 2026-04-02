package pascal.semantic;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
import pascal.error.SemanticError;
import pascal.interpreter.AstVisitor;
import pascal.symboltable.*;

import java.util.List;

/**
 * Семантический анализатор.
 *
 * Проходит AST через паттерн Visitor и выполняет:
 *  1. Построение таблицы символов (ScopedSymbolTable)
 *  2. Проверку объявленности всех переменных
 *  3. Проверку совместимости типов в выражениях и присваиваниях
 *  4. Аннотирование узлов ExpressionNode полем resolvedType
 *
 * Диагностируемые ошибки:
 *  - Переменная не объявлена
 *  - Переменная объявлена дважды
 *  - Несовместимые типы в бинарной операции
 *  - Несовместимые типы при присваивании
 *  - Условие if/while не булевого типа
 *  - Выход за разрешённые типы операндов
 */
public class SemanticAnalyzer implements AstVisitor<String> {

    // Текущая активная область видимости
    private ScopedSymbolTable currentScope;

    /**
     * Запуск анализа. Возвращает корень AST (уже аннотированный).
     */
    public ProgramNode analyze(ProgramNode program) {
        program.accept(this);
        return program;
    }

    // ──────────────────────────────────────────────────────────────
    // Структура программы
    // ──────────────────────────────────────────────────────────────

    @Override
    public String visitProgram(ProgramNode node) {
        System.out.println("[Семантика] Анализ программы: " + node.name());
        // Глобальная область видимости (уровень 1)
        currentScope = new ScopedSymbolTable(node.name(), 1, null);
        node.block().accept(this);
        currentScope.print(); // показываем финальную таблицу символов
        return null;
    }

    @Override
    public String visitBlock(BlockNode node) {
        // Объявления (var)
        for (AstNode decl : node.declarations()) {
            decl.accept(this);
        }
        // Тело блока
        node.compoundStatement().accept(this);
        return null;
    }

    @Override
    public String visitVarDeclaration(VarDeclarationNode node) {
        String typeName = node.type().typeName.toLowerCase();
        for (String varName : node.variableNames()) {
            VariableSymbol sym;
            if (node.type().isArray) {
                sym = new VariableSymbol(varName, typeName,
                        node.type().arrayStartIndex,
                        node.type().arrayEndIndex);
            } else {
                sym = new VariableSymbol(varName, typeName);
            }
            currentScope.define(sym);
            System.out.println("[Семантика] Объявлена переменная: " + sym);
        }
        return null;
    }

    @Override
    public String visitType(TypeNode node) {
        return node.typeName.toLowerCase();
    }

    // ──────────────────────────────────────────────────────────────
    // Операторы
    // ──────────────────────────────────────────────────────────────

    @Override
    public String visitCompoundStatement(CompoundStatementNode node) {
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public String visitAssignment(AssignmentNode node) {
        // Определяем тип левой части
        String leftType;
        if (node.variable instanceof VariableNode vn) {
            leftType = resolveVarType(vn.name);
        } else if (node.variable instanceof ArrayAccessNode an) {
            leftType = resolveArrayElementType(an);
        } else {
            throw new SemanticError("Некорректная левая часть присваивания");
        }

        // Тип правой части (выражение аннотируется внутри)
        String rightType = node.expression.accept(this);

        // Проверка совместимости типов
        if (!typesAssignable(leftType, rightType)) {
            throw new SemanticError("Несовместимые типы при присваивании: ожидается '"
                    + leftType + "', получено '" + rightType + "'");
        }
        return null;
    }

    @Override
    public String visitIf(IfNode node) {
        String condType = node.condition.accept(this);
        if (!"boolean".equals(condType)) {
            throw new SemanticError("Условие в IF должно быть boolean, получено: '" + condType + "'");
        }
        node.thenBranch.accept(this);
        if (node.elseBranch != null) node.elseBranch.accept(this);
        return null;
    }

    @Override
    public String visitWhile(WhileNode node) {
        String condType = node.condition.accept(this);
        if (!"boolean".equals(condType)) {
            throw new SemanticError("Условие в WHILE должно быть boolean, получено: '" + condType + "'");
        }
        node.body.accept(this);
        return null;
    }

    @Override
    public String visitDoWhile(DoWhileNode node) {
        for (StatementNode s : node.body) s.accept(this);
        String condType = node.condition.accept(this);
        if (!"boolean".equals(condType)) {
            throw new SemanticError("Условие в REPEAT-UNTIL должно быть boolean, получено: '" + condType + "'");
        }
        return null;
    }

    @Override
    public String visitFor(ForNode node) {
        // Переменная цикла должна быть объявлена и быть integer
        String varType = resolveVarType(node.variableName);
        if (!"integer".equals(varType)) {
            throw new SemanticError("Переменная цикла FOR '" + node.variableName + "' должна быть integer, получено: '" + varType + "'");
        }
        String startType = node.startValue.accept(this);
        String endType   = node.endValue.accept(this);
        if (!"integer".equals(startType) || !"integer".equals(endType)) {
            throw new SemanticError("Границы цикла FOR должны быть integer");
        }
        node.body.accept(this);
        return null;
    }

    @Override
    public String visitWrite(WriteNode node) {
        for (ExpressionNode e : node.expressions) {
            e.accept(this); // просто проверяем, что выражения корректны
        }
        return null;
    }

    @Override
    public String visitRead(ReadNode node) {
        for (AstNode v : node.variables) {
            if (v instanceof VariableNode vn) {
                resolveVarType(vn.name); // проверяем объявленность
            } else if (v instanceof ArrayAccessNode an) {
                resolveArrayElementType(an);
            }
        }
        return null;
    }

    @Override
    public String visitProcedureCall(ProcedureCallNode node) {
        // Проверяем, что процедура существует
        currentScope.lookup(node.procedureName)
                .orElseThrow(() -> new SemanticError("Процедура '" + node.procedureName + "' не объявлена"));
        for (ExpressionNode arg : node.arguments) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public String visitBreak(BreakNode node) { return null; }

    @Override
    public String visitContinue(ContinueNode node) { return null; }

    // ──────────────────────────────────────────────────────────────
    // Выражения — возвращают строку-тип и аннотируют resolvedType
    // ──────────────────────────────────────────────────────────────

    @Override
    public String visitBinaryOp(BinaryOpNode node) {
        String left  = node.left.accept(this);
        String right = node.right.accept(this);

        String resultType = resolveBinaryOpType(node.operator, left, right);
        node.resolvedType = resultType;
        return resultType;
    }

    @Override
    public String visitUnaryOp(UnaryOpNode node) {
        String operandType = node.operand.accept(this);
        String result;
        switch (node.operator.toLowerCase()) {
            case "-" -> {
                if (!"integer".equals(operandType)) {
                    throw new SemanticError("Унарный минус применим только к integer, получено: '" + operandType + "'");
                }
                result = "integer";
            }
            case "not" -> {
                if (!"boolean".equals(operandType)) {
                    throw new SemanticError("NOT применим только к boolean, получено: '" + operandType + "'");
                }
                result = "boolean";
            }
            default -> throw new SemanticError("Неизвестный унарный оператор: '" + node.operator + "'");
        }
        node.resolvedType = result;
        return result;
    }

    @Override
    public String visitLiteral(LiteralNode node) {
        String type = switch (node.type) {
            case INTEGER -> "integer";
            case BOOLEAN -> "boolean";
            case CHAR    -> "char";
            case STRING  -> "string";
        };
        node.resolvedType = type;
        return type;
    }

    @Override
    public String visitVariable(VariableNode node) {
        String type = resolveVarType(node.name);
        node.resolvedType = type;
        return type;
    }

    @Override
    public String visitArrayAccess(ArrayAccessNode node) {
        String elemType = resolveArrayElementType(node);
        node.resolvedType = elemType;
        return elemType;
    }

    @Override
    public String visitFunctionCall(FunctionCallNode node) {
        Symbol sym = currentScope.lookup(node.functionName)
                .orElseThrow(() -> new SemanticError("Функция '" + node.functionName + "' не объявлена"));
        if (!(sym instanceof FunctionSymbol fn)) {
            throw new SemanticError("'" + node.functionName + "' не является функцией");
        }
        for (ExpressionNode arg : node.arguments) arg.accept(this);
        node.resolvedType = fn.returnType;
        return fn.returnType;
    }

    // ──────────────────────────────────────────────────────────────
    // Вспомогательные методы
    // ──────────────────────────────────────────────────────────────

    /** Получить тип переменной из таблицы символов или бросить SemanticError */
    private String resolveVarType(String name) {
        return currentScope.lookup(name)
                .filter(s -> s instanceof VariableSymbol)
                .map(s -> ((VariableSymbol) s).type)
                .orElseThrow(() -> new SemanticError("Переменная '" + name + "' не объявлена"));
    }

    /** Получить тип элемента массива */
    private String resolveArrayElementType(ArrayAccessNode node) {
        Symbol sym = currentScope.lookup(node.name)
                .orElseThrow(() -> new SemanticError("Переменная '" + node.name + "' не объявлена"));
        if (!(sym instanceof VariableSymbol vs) || !vs.isArray) {
            throw new SemanticError("'" + node.name + "' не является массивом");
        }
        // Проверяем тип индекса
        String idxType = node.index.accept(this);
        if (!"integer".equals(idxType)) {
            throw new SemanticError("Индекс массива '" + node.name + "' должен быть integer, получено: '" + idxType + "'");
        }
        return vs.elementType();
    }

    /**
     * Определяет тип результата бинарной операции.
     * Арифметика:  integer op integer  → integer
     * Сравнение:   integer op integer  → boolean
     * Логика:      boolean op boolean  → boolean
     */
    private String resolveBinaryOpType(String op, String left, String right) {
        return switch (op) {
            case "+", "-", "*", "/", "mod", "div" -> {
                requireType(left, "integer", op);
                requireType(right, "integer", op);
                yield "integer";
            }
            case ">", "<", ">=", "<=", "=", "<>" -> {
                if (!left.equals(right)) {
                    throw new SemanticError("Несовместимые типы в операции сравнения '"
                            + op + "': '" + left + "' и '" + right + "'");
                }
                yield "boolean";
            }
            case "and", "or" -> {
                requireType(left, "boolean", op);
                requireType(right, "boolean", op);
                yield "boolean";
            }
            default -> throw new SemanticError("Неизвестный бинарный оператор: '" + op + "'");
        };
    }

    /** Проверить, что тип совпадает с ожидаемым */
    private void requireType(String actual, String expected, String context) {
        if (!expected.equals(actual)) {
            throw new SemanticError("Ожидался тип '" + expected + "' в операции '" + context
                    + "', получено '" + actual + "'");
        }
    }

    /**
     * Проверка совместимости типов при присваивании.
     * В Pascal типы должны совпадать точно (без неявного приведения).
     */
    private boolean typesAssignable(String leftType, String rightType) {
        return leftType.equals(rightType);
    }
}
