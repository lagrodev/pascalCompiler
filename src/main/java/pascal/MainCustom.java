package pascal;

import pascal.ast.AstNode;
import pascal.custom.Lexer;
import pascal.custom.Parser;
import pascal.custom.Token;
import pascal.util.TreePrinter;

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
                        "       write(x);\n" +
                        "end.";

        System.out.println("=== 1. ЛЕКСИЧЕСКИЙ АНАЛИЗ ===");
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();
        for (Token t : tokens) {
            System.out.println(t);
        }

        System.out.println("\n=== 2. СИНТАКСИЧЕСКИЙ АНАЛИЗ И AST ===");
        Parser parser = new Parser(tokens);
        AstNode ast = parser.parse();

        System.out.println("\n=== 3. ПЕЧАТЬ ДЕРЕВА (TreePrinter) ===");
        TreePrinter printer = new TreePrinter();
        printer.print(ast);
    }
}