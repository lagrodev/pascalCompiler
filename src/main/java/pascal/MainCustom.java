package pascal;

import pascal.ast.ProgramNode;
import pascal.custom.Lexer;
import pascal.custom.Parser;
import pascal.custom.Token;
import pascal.error.CompilerException;
import pascal.semantic.SemanticAnalyzer;
import pascal.util.TreePrinter;
import pascal.vm.VMInterpreter;

import java.util.List;

public class MainCustom {
    public static void main(String[] args) {

        String sourceCode =
                "program Test;\n" +
                "var x, y: integer;\n" +
                "begin\n" +
                "   x := 10;\n" +
                "   y := x + 5 * 2;\n" +
                "   if y > 15 then\n" +
                "       write(y);\n" +
                "end.";

        try {

            System.out.println("=== 1. ЛЕКСИЧЕСКИЙ АНАЛИЗ ===");
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();
            for (Token t : tokens) System.out.println(t);


            System.out.println("\n=== 2. СИНТАКСИЧЕСКИЙ АНАЛИЗ И AST ===");
            Parser parser = new Parser(tokens);
            ProgramNode ast = parser.parse();


            System.out.println("\n=== 3. ПЕЧАТЬ ДЕРЕВА (TreePrinter) ===");
            TreePrinter printer = new TreePrinter();
            printer.print(ast);


            System.out.println("\n=== 4. СЕМАНТИЧЕСКИЙ АНАЛИЗ ===");
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            ProgramNode annotatedAst = analyzer.analyze(ast);
            System.out.println("[Семантика] ✓ Ошибок не обнаружено");


            System.out.println("\n=== 5. КОМПИЛЯЦИЯ В БАЙТКОД И ВЫПОЛНЕНИЕ ===");
            VMInterpreter vm = new VMInterpreter();
            vm.run(annotatedAst, true);  // true = печатать байткод

            System.out.println("\n=== ВЫПОЛНЕНИЕ ЗАВЕРШЕНО ===");

        } catch (CompilerException e) {
            System.err.println("\n[ОШИБКА КОМПИЛЯТОРА] " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\n[НЕПРЕДВИДЕННАЯ ОШИБКА] " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
