package pascal.vm.runtime;

import pascal.error.RuntimeError;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Фрейм вызова (activation record) для виртуальной машины.
 *
 * Содержит:
 *  - operandStack: стек операндов (вычисления выражений)
 *  - variables:    таблица переменных текущего фрейма
 *  - returnAddress: индекс инструкции для возврата
 *  - functionName:  имя функции/процедуры (для отладки)
 */
public class Frame {
    private final Deque<Object> operandStack = new ArrayDeque<>();
    private final Map<String, Object> variables = new HashMap<>();

    public final String functionName;
    public int returnAddress; // куда вернуться после RET

    public Frame(String functionName, int returnAddress) {
        this.functionName  = functionName;
        this.returnAddress = returnAddress;
    }

    // ── Стек операндов ───────────────────────────────────────────

    public void push(Object value) {
        operandStack.push(value);
    }

    public Object pop() {
        if (operandStack.isEmpty()) {
            throw new RuntimeError("Стек пуст — невозможно выполнить pop() в функции '" + functionName + "'");
        }
        return operandStack.pop();
    }

    public Object peek() {
        if (operandStack.isEmpty()) {
            throw new RuntimeError("Стек пуст — невозможно выполнить peek() в функции '" + functionName + "'");
        }
        return operandStack.peek();
    }

    public boolean isStackEmpty() {
        return operandStack.isEmpty();
    }

    public int stackSize() {
        return operandStack.size();
    }

    // ── Переменные ───────────────────────────────────────────────

    public void setVar(String name, Object value) {
        variables.put(name.toLowerCase(), value);
    }

    public Object getVar(String name) {
        String key = name.toLowerCase();
        if (!variables.containsKey(key)) {
            throw new RuntimeError("Переменная '" + name + "' не инициализирована");
        }
        return variables.get(key);
    }

    public boolean hasVar(String name) {
        return variables.containsKey(name.toLowerCase());
    }

    // ── Типизированные вспомогательные методы ────────────────────

    public int popInt() {
        Object v = pop();
        if (v instanceof Integer i) return i;
        throw new RuntimeError("Ожидался integer, получено: " + v.getClass().getSimpleName());
    }

    public boolean popBool() {
        Object v = pop();
        if (v instanceof Boolean b) return b;
        if (v instanceof Integer i) return i != 0;
        throw new RuntimeError("Ожидался boolean, получено: " + v.getClass().getSimpleName());
    }

    public String popString() {
        Object v = pop();
        return String.valueOf(v);
    }

    @Override
    public String toString() {
        return "Frame[" + functionName + "] vars=" + variables + " stack=" + operandStack;
    }
}
