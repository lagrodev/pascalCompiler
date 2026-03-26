package pascal;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import pascal.ast.AstNode;
import pascal.parser.PascalAstBuilder;
import pascal.parser.PascalLexer;
import pascal.parser.PascalParser;
import pascal.util.TreePrinter;

public class Main {
    public static void main(String[] args) {
        // Простая тестовая программа на Паскале
        String sourceCode =
                "program Test;\n" +
                        "var x, y: integer;\n" +
                        "begin\n" +
                        "   x := 10;\n" +
                        "   y := x + 5 * 2;\n" +
                        "   if y > 15 then\n" +
                        "       write('Success');\n" +
                        "end.";

        // Лексический анализ (Текст -> Токены)
        PascalLexer lexer = new PascalLexer(CharStreams.fromString(sourceCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Синтаксический анализ (Токены -> ANTLR ParseTree)
        PascalParser parser = new PascalParser(tokens);
        ParseTree tree = parser.program();

        //  Построение AST
        PascalAstBuilder builder = new PascalAstBuilder();
        AstNode ast = builder.visit(tree);

        // Печать дерева
        TreePrinter printer = new TreePrinter();
        System.out.println("--- Abstract Syntax Tree ---");
        printer.print(ast);
    }
}