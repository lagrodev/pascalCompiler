package pascal.vm.opcode;

/**
 * Одна инструкция VM. Опкод + необязательный аргумент.
 *
 * Аргумент может быть:
 *   Integer  — для PUSH_INT, JMP, JZ, JNZ, SYS_WRITE
 *   Boolean  — для PUSH_BOOL
 *   String   — для LOAD, STORE, PUSH_STRING, CALL, LABEL и т.д.
 *   int[]    — для NEW_ARRAY: {start, end}
 *   null     — если аргумент не нужен
 */
public class Instruction {
    public final Opcode opcode;
    public final Object arg;     // основной аргумент
    public final Object arg2;    // второй аргумент (для NEW_ARRAY: varName + bounds)

    public Instruction(Opcode opcode) {
        this(opcode, null, null);
    }

    public Instruction(Opcode opcode, Object arg) {
        this(opcode, arg, null);
    }

    public Instruction(Opcode opcode, Object arg, Object arg2) {
        this.opcode = opcode;
        this.arg    = arg;
        this.arg2   = arg2;
    }

    // ── Фабричные методы для читаемости ─────────────────────────

    public static Instruction pushInt(int v)       { return new Instruction(Opcode.PUSH_INT, v); }
    public static Instruction pushBool(boolean v)  { return new Instruction(Opcode.PUSH_BOOL, v); }
    public static Instruction pushStr(String v)    { return new Instruction(Opcode.PUSH_STRING, v); }
    public static Instruction load(String name)    { return new Instruction(Opcode.LOAD, name); }
    public static Instruction store(String name)   { return new Instruction(Opcode.STORE, name); }
    public static Instruction jmp(String label)    { return new Instruction(Opcode.JMP, label); }
    public static Instruction jz(String label)     { return new Instruction(Opcode.JZ, label); }
    public static Instruction jnz(String label)    { return new Instruction(Opcode.JNZ, label); }
    public static Instruction label(String name)   { return new Instruction(Opcode.LABEL, name); }
    public static Instruction call(String name, int argc) { return new Instruction(Opcode.CALL, name, argc); }
    public static Instruction sysWrite(int n)      { return new Instruction(Opcode.SYS_WRITE, n); }
    public static Instruction sysWriteln(int n)    { return new Instruction(Opcode.SYS_WRITELN, n); }
    public static Instruction arrayLoad(String n)  { return new Instruction(Opcode.ARRAY_LOAD, n); }
    public static Instruction arrayStore(String n) { return new Instruction(Opcode.ARRAY_STORE, n); }
    public static Instruction newArray(String name, int start, int end) {
        return new Instruction(Opcode.NEW_ARRAY, name, new int[]{start, end});
    }

    @Override
    public String toString() {
        if (arg == null)  return String.format("%-14s", opcode);
        if (arg2 == null) return String.format("%-14s %s", opcode, argStr(arg));
        return String.format("%-14s %s, %s", opcode, argStr(arg), argStr(arg2));
    }

    private String argStr(Object o) {
        if (o instanceof int[] a) return "[" + a[0] + ".." + a[1] + "]";
        return String.valueOf(o);
    }
}
