package pascal.vm.compiler;

import pascal.ast.*;
import pascal.ast.declarationImpl.VarDeclarationNode;
import pascal.ast.expressionImpl.*;
import pascal.ast.statementImpl.*;
import pascal.interpreter.AstVisitor;
import pascal.vm.opcode.Instruction;
import pascal.vm.opcode.Opcode;

import java.util.ArrayList;
import java.util.List;

/**
 * Компилятор AST → байткод VM.
 *
 * Использует паттерн Visitor. Обходит AST и генерирует список инструкций.
 * Метки (LABEL) используются как символические адреса; VM при загрузке
 * выполняет их разрешение в числовые индексы.
 *
 * Поддерживаемые конструкции:
 *   - var-объявления (простые переменные и массивы)
 *   - присваивание (переменная и элемент массива)
 *   - арифметика, сравнение, логика, унарные операции
 *   - if / if-else
 *   - while, repeat-until, for (to/downto)
 *   - write / writeln
 *   - read (из stdin)
 *   - break / continue (через стек меток)
 */
public class BytecodeCompiler implements AstVisitor<Void> {

    private final List<Instruction> code = new ArrayList<>();
    private int labelCounter = 0;

    // Стеки меток для break/continue внутри циклов
    private final java.util.Deque<String> breakLabels    = new java.util.ArrayDeque<>();
    private final java.util.Deque<String> continueLabels = new java.util.ArrayDeque<>();

    // ── Публичный API ────────────────────────────────────────────

    /** Скомпилировать AST-программу, вернуть список инструкций */
    public List<Instruction> compile(ProgramNode program) {
        program.accept(this);
        emit(Opcode.HALT);
        resolveLabels();
        return code;
    }

    /** Распечатать дизассемблированный байткод */
    public void printCode() {
        System.out.println("\n=== БАЙТКОД ===");
        for (int i = 0; i < code.size(); i++) {
            System.out.printf("%4d  %s%n", i, code.get(i));
        }
    }

    // ── Генерация инструкций ─────────────────────────────────────

    private void emit(Opcode op) {
        code.add(new Instruction(op));
    }

    private void emit(Instruction instr) {
        code.add(instr);
    }

    private String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    // ── Visitor — структура программы ────────────────────────────

    @Override
    public Void visitProgram(ProgramNode node) {
        node.block().accept(this);
        return null;
    }

    @Override
    public Void visitBlock(BlockNode node) {
        for (AstNode decl : node.declarations()) decl.accept(this);
        node.compoundStatement().accept(this);
        return null;
    }

    @Override
    public Void visitVarDeclaration(VarDeclarationNode node) {
        for (String name : node.variableNames()) {
            if (node.type().isArray) {
                // NEW_ARRAY создаёт массив в фрейме
                emit(Instruction.newArray(name,
                        node.type().arrayStartIndex,
                        node.type().arrayEndIndex));
            } else {
                // Инициализация нулём / false / ""
                switch (node.type().typeName.toLowerCase()) {
                    case "integer" -> emit(Instruction.pushInt(0));
                    case "boolean" -> emit(Instruction.pushBool(false));
                    default        -> emit(Instruction.pushStr(""));
                }
                emit(Instruction.store(name));
            }
        }
        return null;
    }

    @Override
    public Void visitType(TypeNode node) { return null; }

    // ── Visitor — операторы ──────────────────────────────────────

    @Override
    public Void visitCompoundStatement(CompoundStatementNode node) {
        for (StatementNode s : node.statements) s.accept(this);
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentNode node) {
        if (node.variable instanceof ArrayAccessNode an) {
            // Для arr[i] := expr: нужно положить индекс, потом значение
            an.index.accept(this);    // index на стек
            node.expression.accept(this); // value на стек
            emit(Instruction.arrayStore(an.name));
        } else if (node.variable instanceof VariableNode vn) {
            node.expression.accept(this);
            emit(Instruction.store(vn.name));
        }
        return null;
    }

    @Override
    public Void visitIf(IfNode node) {
        String labelElse = newLabel("else");
        String labelEnd  = newLabel("endif");

        node.condition.accept(this);
        emit(Instruction.jz(labelElse));

        node.thenBranch.accept(this);

        if (node.elseBranch != null) {
            emit(Instruction.jmp(labelEnd));
            emit(Instruction.label(labelElse));
            node.elseBranch.accept(this);
            emit(Instruction.label(labelEnd));
        } else {
            emit(Instruction.label(labelElse));
        }
        return null;
    }

    @Override
    public Void visitWhile(WhileNode node) {
        String labelStart = newLabel("while_start");
        String labelEnd   = newLabel("while_end");

        breakLabels.push(labelEnd);
        continueLabels.push(labelStart);

        emit(Instruction.label(labelStart));
        node.condition.accept(this);
        emit(Instruction.jz(labelEnd));
        node.body.accept(this);
        emit(Instruction.jmp(labelStart));
        emit(Instruction.label(labelEnd));

        breakLabels.pop();
        continueLabels.pop();
        return null;
    }

    @Override
    public Void visitDoWhile(DoWhileNode node) {
        String labelStart = newLabel("repeat_start");
        String labelEnd   = newLabel("repeat_end");

        breakLabels.push(labelEnd);
        continueLabels.push(labelStart);

        emit(Instruction.label(labelStart));
        for (StatementNode s : node.body) s.accept(this);
        node.condition.accept(this);
        emit(Instruction.jz(labelStart));  // repeat until condition: прыгаем назад если НЕ выполнено
        emit(Instruction.label(labelEnd));

        breakLabels.pop();
        continueLabels.pop();
        return null;
    }

    @Override
    public Void visitFor(ForNode node) {
        /*
         * Pascal: for i := start to end do body
         * Эквивалент:
         *   i := start
         *   while i <= end do begin body; i := i + 1 end
         */
        String labelStart = newLabel("for_start");
        String labelEnd   = newLabel("for_end");
        int step = node.isTo ? 1 : -1;
        String cmpOp = node.isTo ? "<=" : ">=";

        // i := start
        node.startValue.accept(this);
        emit(Instruction.store(node.variableName));

        breakLabels.push(labelEnd);
        continueLabels.push(labelStart);

        emit(Instruction.label(labelStart));

        // Условие: i <= end (или i >= end для downto)
        emit(Instruction.load(node.variableName));
        node.endValue.accept(this);
        emit(new Instruction(cmpOpcodeFor(cmpOp)));
        emit(Instruction.jz(labelEnd));

        // Тело
        node.body.accept(this);

        // i := i + step
        emit(Instruction.load(node.variableName));
        emit(Instruction.pushInt(Math.abs(step)));
        emit(new Instruction(step > 0 ? Opcode.OP_ADD : Opcode.OP_SUB));
        emit(Instruction.store(node.variableName));

        emit(Instruction.jmp(labelStart));
        emit(Instruction.label(labelEnd));

        breakLabels.pop();
        continueLabels.pop();
        return null;
    }

    @Override
    public Void visitWrite(WriteNode node) {
        for (ExpressionNode e : node.expressions) e.accept(this);
        if (node.newLine) {
            emit(Instruction.sysWriteln(node.expressions.size()));
        } else {
            emit(Instruction.sysWrite(node.expressions.size()));
        }
        return null;
    }

    @Override
    public Void visitRead(ReadNode node) {
        for (AstNode v : node.variables) {
            String name = (v instanceof VariableNode vn) ? vn.name : ((ArrayAccessNode) v).name;
            emit(new Instruction(Opcode.SYS_READ, name));
        }
        return null;
    }

    @Override
    public Void visitProcedureCall(ProcedureCallNode node) {
        String pname = node.procedureName.toLowerCase();
        // Встроенные: write / writeln → SYS_WRITE / SYS_WRITELN
        if (pname.equals("write") || pname.equals("writeln")) {
            for (ExpressionNode e : node.arguments) e.accept(this);
            Opcode sysOp = pname.equals("writeln") ? Opcode.SYS_WRITELN : Opcode.SYS_WRITE;
            code.add(new Instruction(sysOp, node.arguments.size()));
        } else {
            for (ExpressionNode e : node.arguments) e.accept(this);
            emit(Instruction.call(node.procedureName, node.arguments.size()));
        }
        return null;
    }

    @Override
    public Void visitBreak(BreakNode node) {
        if (breakLabels.isEmpty()) throw new RuntimeException("break вне цикла");
        emit(Instruction.jmp(breakLabels.peek()));
        return null;
    }

    @Override
    public Void visitContinue(ContinueNode node) {
        if (continueLabels.isEmpty()) throw new RuntimeException("continue вне цикла");
        emit(Instruction.jmp(continueLabels.peek()));
        return null;
    }

    // ── Visitor — выражения ──────────────────────────────────────

    @Override
    public Void visitBinaryOp(BinaryOpNode node) {
        node.left.accept(this);
        node.right.accept(this);
        emit(new Instruction(binaryOpcode(node.operator)));
        return null;
    }

    @Override
    public Void visitUnaryOp(UnaryOpNode node) {
        node.operand.accept(this);
        switch (node.operator.toLowerCase()) {
            case "-"   -> emit(new Instruction(Opcode.OP_NEG));
            case "not" -> emit(new Instruction(Opcode.OP_NOT));
            default    -> throw new RuntimeException("Неизвестный унарный оператор: " + node.operator);
        }
        return null;
    }

    @Override
    public Void visitLiteral(LiteralNode node) {
        switch (node.type) {
            case INTEGER -> emit(Instruction.pushInt((Integer) node.value));
            case BOOLEAN -> emit(Instruction.pushBool((Boolean) node.value));
            case STRING  -> emit(Instruction.pushStr((String) node.value));
            case CHAR    -> emit(Instruction.pushStr(String.valueOf(node.value)));
        }
        return null;
    }

    @Override
    public Void visitVariable(VariableNode node) {
        emit(Instruction.load(node.name));
        return null;
    }

    @Override
    public Void visitArrayAccess(ArrayAccessNode node) {
        node.index.accept(this);
        emit(Instruction.arrayLoad(node.name));
        return null;
    }

    @Override
    public Void visitFunctionCall(FunctionCallNode node) {
        for (ExpressionNode arg : node.arguments) arg.accept(this);
        emit(Instruction.call(node.functionName, node.arguments.size()));
        return null;
    }

    // ── Вспомогательные ─────────────────────────────────────────

    private Opcode binaryOpcode(String op) {
        return switch (op) {
            case "+"  -> Opcode.OP_ADD;
            case "-"  -> Opcode.OP_SUB;
            case "*"  -> Opcode.OP_MUL;
            case "/"  -> Opcode.OP_DIV;
            case "mod" -> Opcode.OP_MOD;
            case "="  -> Opcode.CMP_EQ;
            case "<>" -> Opcode.CMP_NE;
            case "<"  -> Opcode.CMP_LT;
            case "<=" -> Opcode.CMP_LE;
            case ">"  -> Opcode.CMP_GT;
            case ">=" -> Opcode.CMP_GE;
            case "and"-> Opcode.OP_AND;
            case "or" -> Opcode.OP_OR;
            default   -> throw new RuntimeException("Неизвестный оператор: " + op);
        };
    }

    private Opcode cmpOpcodeFor(String op) {
        return switch (op) {
            case "<=" -> Opcode.CMP_LE;
            case ">=" -> Opcode.CMP_GE;
            default   -> throw new RuntimeException("Неожиданный оператор: " + op);
        };
    }

    /**
     * Разрешение символических меток в числовые адреса.
     * Проход 1: собрать адреса LABEL-инструкций.
     * Проход 2: заменить строковые аргументы JMP/JZ/JNZ на Integer.
     * LABEL-инструкции остаются в коде (VM их игнорирует через NOP).
     */
    private void resolveLabels() {
        // Проход 1: собрать таблицу меток
        java.util.Map<String, Integer> labelMap = new java.util.HashMap<>();
        for (int i = 0; i < code.size(); i++) {
            Instruction ins = code.get(i);
            if (ins.opcode == Opcode.LABEL) {
                labelMap.put((String) ins.arg, i);
            }
        }
        // Проход 2: заменить метки на адреса
        for (int i = 0; i < code.size(); i++) {
            Instruction ins = code.get(i);
            if ((ins.opcode == Opcode.JMP || ins.opcode == Opcode.JZ || ins.opcode == Opcode.JNZ)
                    && ins.arg instanceof String label) {
                if (!labelMap.containsKey(label)) {
                    throw new RuntimeException("Неизвестная метка: " + label);
                }
                code.set(i, new Instruction(ins.opcode, labelMap.get(label)));
            }
        }
    }
}
