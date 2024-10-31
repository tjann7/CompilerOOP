package ru.team.compiler.tree;

import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.analyzer.Analyzer;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.NodeToStringHelper;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.StatementNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TreeMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ' to exit, | to parse");
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String code = scanner.nextLine();
            if (code.strip().equals("'")) {
                break;
            }

            if (code.strip().equals("|")) {
                try {
                    Tokenizer tokenizer = new Tokenizer(stringBuilder.toString());
                    List<Token> tokens = new ArrayList<>();
                    while (tokenizer.hasNext()) {
                        tokens.add(tokenizer.next());
                    }

                    TreeNode node = ProgramNode.PARSER.parse(tokens);
                    System.out.println(NodeToStringHelper.toString(node, true));

                    TreeNode analyzeNode = node;
                    if (analyzeNode instanceof ClassNode classNode) {
                        analyzeNode = new ProgramNode(List.of(classNode));
                    }

                    try {
                        AnalyzeContext context;
                        if (analyzeNode instanceof ProgramNode programNode) {
                            context = Analyzer.createContext(programNode);
                        } else {
                            context = Analyzer.createContext(new ProgramNode(new ArrayList<>()));
                        }

                        node.analyze(context);

                        System.out.println("\n---\nNode was traversed successfully!\n---");
                    } catch (Exception e) {
                        System.out.println("\n---\n[!] Node was not traversed: " + e + "\n---");
                    }

                    TreeNode optimized = null;
                    if (node instanceof ProgramNode programNode) {
                        optimized = programNode.optimize();
                    } else if (node instanceof ClassNode classNode) {
                        optimized = classNode.optimize();
                    } else if (node instanceof ClassMemberNode classMemberNode) {
                        optimized = classMemberNode.optimize();
                    } else if (node instanceof BodyNode bodyNode) {
                        optimized = bodyNode.optimize();
                    } else if (node instanceof StatementNode statementNode) {
                        List<StatementNode> optimize = statementNode.optimize();
                        optimized = optimize.size() == 1 ? optimize.get(0) : new BodyNode(optimize);
                    }

                    if (optimized != null) {
                        if (optimized.equals(node)) {
                            System.out.println("\nNothing to optimize\n");
                        } else {
                            System.out.println("\nOptimized:\n");
                            System.out.println(NodeToStringHelper.toString(optimized, true));
                            System.out.println();
                        }
                    } else {
                        System.out.println("\nOptimization is not supported\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stringBuilder = new StringBuilder();

                continue;
            }

            stringBuilder.append(code).append("\n");
        }
    }
}
