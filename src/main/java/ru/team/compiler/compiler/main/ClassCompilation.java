package ru.team.compiler.compiler.main;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.analyzer.Analyzer;
import ru.team.compiler.compiler.ClassFile;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ProgramNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ClassCompilation {

    private ClassCompilation() {

    }

    public static void compile(@NotNull Path path) {
        if (Files.isDirectory(path)) {
            System.err.println("[ERROR] " + path + " | Must be file");
            return;
        }

        Path outputPath = path.resolveSibling("out");

        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            System.err.println("[ERROR] " + outputPath + " | Failed on directory creation: " + e);
            return;
        }

        String string;
        try {
            string = Files.readString(path);
        } catch (IOException e) {
            System.err.println("[ERROR] " + outputPath + " | Failed on file reading: " + e);
            return;
        }

        List<Token> tokens = new ArrayList<>();

        try {
            Tokenizer tokenizer = new Tokenizer(string);
            while (tokenizer.hasNext()) {
                tokens.add(tokenizer.next());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + path + " | Failed on tokenization: " + e);
            return;
        }

        ProgramNode programNode;
        try {
            programNode = ProgramNode.PARSER.parse(tokens);
        } catch (Exception e) {
            System.err.println("[ERROR] " + path + " | Failed on syntax analysis: " + e);
            return;
        }

        AnalyzeContext context;
        try {
            context = Analyzer.createContext(path.getParent(), programNode);
            programNode.analyze(context);
        } catch (Exception e) {
            System.err.println("[ERROR] " + path + " | Failed on semantic analysis: " + e);
            return;
        }

        try {
            programNode = programNode.optimize();
        } catch (Exception e) {
            System.err.println("[ERROR] " + path + " | Failed on optimization: " + e);
            return;
        }

        // ---

        for (ClassNode classNode : programNode.classes()) {
            Path classOutputPath = outputPath.resolve(classNode.name().value() + ".class");

            try (OutputStream outputStream = Files.newOutputStream(classOutputPath);
                 DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

                ClassFile classFile = ClassFile.fromNode(context, classNode);

                classFile.compile(new CompilationContext(context), dataOutputStream);
            } catch (Exception e) {
                System.err.println("[ERROR] " + path + " | Failed on compilation: " + e);
                return;
            }
        }
    }
}
