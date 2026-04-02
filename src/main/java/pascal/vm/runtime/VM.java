package pascal.vm.runtime;

import pascal.error.RuntimeError;
import pascal.vm.opcode.Instruction;
import pascal.vm.opcode.Opcode;

import java.util.*;

/**
 * Стековая виртуальная машина для выполнения Pascal-программ.
 *
 * Архитектура:
 *  - callStack: стек фреймов вызовов
 *  - ip (instruction pointer): текущая инструкция
 *  - Один глобальный фрейм для переменных программы
 *
 * VM выполняет список инструкций, сгенерированных BytecodeCompiler.
 */
public class VM {

    private final List<Instruction> code;
    private int ip = 0;

    // Стек фреймов: вершина — текущий активный фрейм
    private final Deque<Frame> callStack = new ArrayDeque<>();

    // Глобальный фрейм (переменные программы)
    private Frame globalFrame;

    // Таблица массивов: varName → ArrayValue
    private final Map<String, ArrayValue> arrays = new HashMap<>();

    // Таблица пользовательских функций: name → стартовый адрес (для расширения)
    private final Map<String, Integer> functions = new HashMap<>();

    public VM(List<Instruction> code) {
        this.code = code;
    }

    /** Запустить программу */
    public void run() {
        globalFrame = new Frame("<global>", -1);
        callStack.push(globalFrame);

        while (ip < code.size()) {
            Instruction ins = code.get(ip);
            ip++;
            execute(ins);
        }
    }

    private Frame frame() {
        return callStack.peek();
    }

    // ── Диспетчер инструкций ─────────────────────────────────────

    private void execute(Instruction ins) {
        switch (ins.opcode) {

            // ── Стек ──────────────────────────────────────────────
            case PUSH_INT    -> frame().push(ins.arg);
            case PUSH_BOOL   -> frame().push((Boolean) ins.arg ? 1 : 0);
            case PUSH_STRING -> frame().push(ins.arg);
            case POP         -> frame().pop();
            case DUP         -> frame().push(frame().peek());

            // ── Переменные ────────────────────────────────────────
            case STORE -> {
                String name = (String) ins.arg;
                frame().setVar(name, frame().pop());
            }
            case LOAD -> {
                String name = (String) ins.arg;
                // Сначала ищем в текущем фрейме, потом в глобальном
                if (frame().hasVar(name)) {
                    frame().push(frame().getVar(name));
                } else if (globalFrame.hasVar(name)) {
                    frame().push(globalFrame.getVar(name));
                } else {
                    throw new RuntimeError("Переменная '" + name + "' не найдена");
                }
            }

            // ── Массивы ───────────────────────────────────────────
            case NEW_ARRAY -> {
                String arrName = (String) ins.arg;
                int[] bounds   = (int[]) ins.arg2;
                // Тип элемента по умолчанию integer (будет расширено при полной типизации)
                arrays.put(arrName.toLowerCase(), new ArrayValue(bounds[0], bounds[1], "integer"));
            }
            case ARRAY_LOAD -> {
                String arrName = ((String) ins.arg).toLowerCase();
                int idx = frame().popInt();
                ArrayValue arr = getArray(arrName);
                frame().push(arr.get(idx));
            }
            case ARRAY_STORE -> {
                String arrName = ((String) ins.arg).toLowerCase();
                Object value   = frame().pop();
                int idx        = frame().popInt();
                getArray(arrName).set(idx, value);
            }

            // ── Арифметика ────────────────────────────────────────
            case OP_ADD -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a + b); }
            case OP_SUB -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a - b); }
            case OP_MUL -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a * b); }
            case OP_DIV -> {
                int b = frame().popInt();
                int a = frame().popInt();
                if (b == 0) throw new RuntimeError("Деление на ноль");
                frame().push(a / b);
            }
            case OP_MOD -> {
                int b = frame().popInt();
                int a = frame().popInt();
                if (b == 0) throw new RuntimeError("Деление на ноль (mod)");
                frame().push(a % b);
            }
            case OP_NEG -> frame().push(-frame().popInt());

            // ── Сравнение → boolean ───────────────────────────────
            case CMP_EQ -> { Object b = frame().pop(), a = frame().pop(); frame().push(objEquals(a, b) ? 1 : 0); }
            case CMP_NE -> { Object b = frame().pop(), a = frame().pop(); frame().push(!objEquals(a, b) ? 1 : 0); }
            case CMP_LT -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a < b  ? 1 : 0); }
            case CMP_LE -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a <= b ? 1 : 0); }
            case CMP_GT -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a > b  ? 1 : 0); }
            case CMP_GE -> { int b = frame().popInt(), a = frame().popInt(); frame().push(a >= b ? 1 : 0); }

            // ── Логика ────────────────────────────────────────────
            case OP_AND -> { boolean b = toBool(frame().pop()), a = toBool(frame().pop()); frame().push(a && b ? 1 : 0); }
            case OP_OR  -> { boolean b = toBool(frame().pop()), a = toBool(frame().pop()); frame().push(a || b ? 1 : 0); }
            case OP_NOT -> frame().push(toBool(frame().pop()) ? 0 : 1);

            // ── Переходы ──────────────────────────────────────────
            case JMP  -> ip = (Integer) ins.arg;
            case JZ   -> { if (!toBool(frame().pop())) ip = (Integer) ins.arg; }
            case JNZ  -> { if ( toBool(frame().pop())) ip = (Integer) ins.arg; }

            // ── Ввод / вывод ──────────────────────────────────────
            case SYS_WRITE -> {
                int count = (Integer) ins.arg;
                printValues(count, false);
            }
            case SYS_WRITELN -> {
                int count = (Integer) ins.arg;
                printValues(count, true);
            }
            case SYS_READ -> {
                String varName = (String) ins.arg;
                Scanner sc = new Scanner(System.in);
                String input = sc.nextLine().trim();
                Object value;
                try { value = Integer.parseInt(input); }
                catch (NumberFormatException e) {
                    if (input.equalsIgnoreCase("true"))  value = true;
                    else if (input.equalsIgnoreCase("false")) value = false;
                    else value = input;
                }
                frame().setVar(varName, value);
            }

            // ── Функции ───────────────────────────────────────────
            case CALL -> {
                // Базовая поддержка вызовов пользовательских функций
                String funcName = ((String) ins.arg).toLowerCase();
                if (functions.containsKey(funcName)) {
                    Frame newFrame = new Frame(funcName, ip);
                    callStack.push(newFrame);
                    ip = functions.get(funcName);
                } else {
                    throw new RuntimeError("Функция/процедура '" + ins.arg + "' не найдена");
                }
            }
            case RET -> {
                Frame done = callStack.pop();
                ip = done.returnAddress;
            }
            case RET_VAL -> {
                Object retVal = frame().pop();
                Frame done = callStack.pop();
                ip = done.returnAddress;
                frame().push(retVal);
            }

            // ── Прочее ────────────────────────────────────────────
            case NOP, LABEL -> { /* ничего */ }
            case HALT       -> ip = code.size(); // завершить цикл
        }
    }

    // ── Вспомогательные ──────────────────────────────────────────

    private void printValues(int count, boolean newLine) {
        // Значения лежат на стеке в обратном порядке — собираем список
        List<Object> vals = new ArrayList<>();
        for (int i = 0; i < count; i++) vals.add(0, frame().pop());
        StringBuilder sb = new StringBuilder();
        for (Object v : vals) sb.append(formatValue(v));
        if (newLine) System.out.println(sb);
        else         System.out.print(sb);
    }

    private String formatValue(Object v) {
        if (v instanceof Boolean b) return b ? "TRUE" : "FALSE";
        // Целочисленное представление boolean не форматируем как TRUE/FALSE
        // (пользователь сам решает что выводить через write)
        return String.valueOf(v);
    }

    private boolean toBool(Object v) {
        if (v instanceof Boolean b)  return b;
        if (v instanceof Integer i)  return i != 0;
        throw new RuntimeError("Ожидался boolean или integer-флаг, получено: " + v);
    }

    private boolean objEquals(Object a, Object b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private ArrayValue getArray(String name) {
        ArrayValue arr = arrays.get(name.toLowerCase());
        if (arr == null) throw new RuntimeError("Массив '" + name + "' не инициализирован");
        return arr;
    }

    /** Зарегистрировать пользовательскую функцию (для расширения) */
    public void registerFunction(String name, int startIp) {
        functions.put(name.toLowerCase(), startIp);
    }
}
