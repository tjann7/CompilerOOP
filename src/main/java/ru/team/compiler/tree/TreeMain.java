package ru.team.compiler.tree;

import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.analyzer.Analyzer;
import ru.team.compiler.compiler.ClassFile;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.NodeToStringHelper;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.StatementNode;
import ru.team.compiler.util.Color;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class TreeMain {

    private TreeMain() {

    }

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

                    TreeNode node = TreeNode.PARSER.parse(tokens);
                    System.out.println(NodeToStringHelper.toString(node, true));

                    TreeNode analyzeNode = node;
                    if (analyzeNode instanceof ClassNode classNode) {
                        analyzeNode = new ProgramNode(List.of(classNode));
                    }

                    AnalyzeContext context;

                    try {
                        if (analyzeNode instanceof ProgramNode programNode) {
                            context = Analyzer.createContext(programNode);
                        } else {
                            context = Analyzer.createContext(new ProgramNode(new ArrayList<>()));
                        }

                        node.analyze(context);

                        System.out.println("\n---\nNode was traversed successfully!\n---");
                    } catch (Exception e) {
                        context = null;

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

                    if (context != null) {
                        ProgramNode compileNode;
                        if (optimized instanceof ProgramNode programNode) {
                            compileNode = programNode.optimize();
                        } else if (optimized instanceof ClassNode classNode) {
                            compileNode = new ProgramNode(List.of(classNode));
                        } else {
                            compileNode = null;
                        }

                        if (compileNode != null) {
                            for (ClassNode classNode : compileNode.classes()) {
                                String name = classNode.name().value();

                                Path path = Path.of("out/" + name + ".class");
                                Files.createDirectories(path.getParent());

                                try (OutputStream outputStream = Files.newOutputStream(path);
                                     DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

                                    ClassFile classFile = ClassFile.fromNode(context, classNode);

                                    classFile.compile(new CompilationContext(context), dataOutputStream);

                                    System.out.println("---\n\nCompiled to: " + name + ".class");

                                    try (InputStream inputStream = Files.newInputStream(path)) {
                                        System.out.println();
                                        int i = 0;

                                        while (inputStream.available() > 0) {
                                            if (i > 0 && i % 10 == 0) {
                                                System.out.println();
                                            }

                                            int b = inputStream.read();
                                            String hex = Integer.toString(b, 16);

                                            String space = " ".repeat(1 + 2 - hex.length());
                                            String s = hex + space + b;

                                            System.out.print(Color.BLACK.code() + hex.toUpperCase() + space
                                                    + Color.PURPLE.code() + b + " ".repeat(9 - s.length()));

                                            i++;
                                        }

                                        System.out.println("\n");
                                    }
                                } catch (Exception e) {
                                    System.out.println("---\n\n[!] Class " + name + " was not compiled: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
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
