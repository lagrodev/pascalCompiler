package pascal.vm;

import pascal.ast.ProgramNode;
import pascal.vm.compiler.BytecodeCompiler;
import pascal.vm.opcode.Instruction;
import pascal.vm.runtime.VM;

import java.util.List;

/**
 * Фасад для компиляции и исполнения Pascal-программы через VM.
 *
 * Использование:
 *   VMInterpreter vm = new VMInterpreter();
 *   vm.run(programNode);          // компилирует и сразу выполняет
 *   vm.run(programNode, true);    // компилирует, печатает байткод, выполняет
 */
public class VMInterpreter {

    private List<Instruction> bytecode;

    /**
     * Скомпилировать AST в байткод и выполнить.
     * @param program    AST-дерево (уже прошедшее семантику)
     * @param printCode  печатать дизассемблированный байткод перед запуском
     */
    public void run(ProgramNode program, boolean printCode) {
        // 1. Компиляция AST → байткод
        BytecodeCompiler compiler = new BytecodeCompiler();
        bytecode = compiler.compile(program);

        // 2. Вывод байткода (опционально)
        if (printCode) compiler.printCode();

        // 3. Выполнение
        VM vm = new VM(bytecode);
        vm.run();
    }

    public void run(ProgramNode program) {
        run(program, false);
    }

    public List<Instruction> getBytecode() {
        return bytecode;
    }
}
