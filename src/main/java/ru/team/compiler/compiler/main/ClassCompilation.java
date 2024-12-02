package ru.team.compiler.compiler.main;

import olang.Any;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.analyzer.Analyzer;
import ru.team.compiler.compiler.ClassFile;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.IncludeNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.util.GeneralUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class ClassCompilation {

    private ClassCompilation() {

    }

    public static void compile(@NotNull Path path, @NotNull Set<String> options) {
        boolean jar = options.contains("-jar");
        boolean bundle = options.contains("-bundle");

        if (Files.isDirectory(path)) {
            System.err.println("[ERROR] " + path + " | Must be file");
            return;
        }

        Path outputPath = path.resolveSibling("out");
        if (!jar) {
            try {
                Files.createDirectories(outputPath);
            } catch (IOException e) {
                System.err.println("[ERROR] " + outputPath + " | Failed on directory creation: " + e);
                return;
            }
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

        if (bundle) {
            programNode = flatProgramNode(path.getParent(), programNode);
        }

        AnalyzeContext context;
        try {
            context = Analyzer.createContext(path.toAbsolutePath().getParent(), programNode);
            context = programNode.analyze(context);

            List<Exception> exceptions = context.exceptions();
            if (!exceptions.isEmpty()) {
                System.err.println("[ERROR] " + path + " | Failed on semantic analysis:\n\n");
                for (Exception exception : exceptions) {
                    System.out.println(exception);
                }
                return;
            }
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

        FileSystem fileSystem = null;
        Function<String, Path> classOutputPathFunction;
        try {
            if (jar) {
                Path jarOutputPath = Path.of(path + (bundle ? ".bundle" : "")+ ".jar");
                try {
                    Files.deleteIfExists(jarOutputPath);
                } catch (IOException e) {
                    System.err.println("[ERROR] " + jarOutputPath + " | Failed on old jar deletion: " + e);
                    return;
                }

                Map<String, String> env = new HashMap<>();
                env.put("create", "true");

                try {
                    URI uri = new URI("jar:" + jarOutputPath.toAbsolutePath().toUri() + "!/");

                    fileSystem = FileSystems.newFileSystem(uri, env);

                    Files.createDirectories(fileSystem.getPath("/olang/"));

                    FileSystem finalFileSystem = fileSystem;
                    classOutputPathFunction = name -> {
                        return finalFileSystem.getPath("/olang/").resolve(name);
                    };
                } catch (URISyntaxException | IOException e) {
                    System.err.println("[ERROR] " + jarOutputPath + " | Failed on jar create: " + e);
                    return;
                }
            } else {
                classOutputPathFunction = outputPath::resolve;
            }

            if (bundle) {
                File file = GeneralUtils.getDeclaringJarFile(Any.class);

                try {
                    URI uri = new URI("jar:" + file.toURI() + "!/");
                    try (FileSystem stdFileSystem = FileSystems.newFileSystem(uri, Map.of());
                         DirectoryStream<Path> stream = Files.newDirectoryStream(
                                 stdFileSystem.getPath("/olang/"), "*.class")) {

                        for (Path stdPath : stream) {
                            Path classOutputPath = classOutputPathFunction.apply(stdPath.getFileName().toString());

                            Files.copy(stdPath, classOutputPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (URISyntaxException | IOException e) {
                    System.err.println("[ERROR] " + path + " | Failed on std bundling: " + e);
                    return;
                }
            }

            for (ClassNode classNode : programNode.classes()) {
                Path classOutputPath = classOutputPathFunction.apply(classNode.name().value() + ".class");

                try (OutputStream outputStream = Files.newOutputStream(classOutputPath);
                     DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {

                    ClassFile classFile = ClassFile.fromNode(context, classNode);

                    classFile.compile(new CompilationContext(context), dataOutputStream);
                } catch (Exception e) {
                    System.err.println("[ERROR] " + path + " | Failed on compilation: " + e);
                    return;
                }
            }
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    System.err.println("[ERROR] " + path + " | Failed on jar close: " + e);
                }
            }
        }
    }

    @NotNull
    public static ProgramNode flatProgramNode(@NotNull Path path, @NotNull ProgramNode programNode) {
        Set<Path> visited = new HashSet<>();
        visited.add(path.toAbsolutePath());
        return flatProgramNode(path, programNode, visited);
    }

    @NotNull
    private static ProgramNode flatProgramNode(@NotNull Path path, @NotNull ProgramNode programNode,
                                               @NotNull Set<Path> visited) {
        List<ClassNode> newClassNodes = new ArrayList<>();

        for (IncludeNode includeNode : programNode.includeNodes()) {
            ReferenceNode referenceNode = includeNode.fileName();
            Path includePath = path.resolve(referenceNode.value() + ".olang");

            if (!visited.add(includePath.toAbsolutePath())) {
                throw new AnalyzerException("Program includes '%s' that includes it too at '%s'"
                        .formatted(referenceNode.value(), includePath));
            }

            if (!Files.exists(includePath)) {
                throw new AnalyzerException("Program includes '%s' that cannot be found at '%s'"
                        .formatted(referenceNode.value(), includePath));
            }

            if (Files.isDirectory(includePath)) {
                throw new AnalyzerException("Program includes '%s' that is not a file at '%s'"
                        .formatted(referenceNode.value(), includePath));
            }

            String string;
            try {
                string = Files.readString(includePath);
            } catch (IOException e) {
                throw new AnalyzerException("Program includes '%s' that could not be read from '%s': %s"
                        .formatted(referenceNode.value(), includePath, e));
            }

            List<Token> tokens = new ArrayList<>();

            try {
                Tokenizer tokenizer = new Tokenizer(string);
                while (tokenizer.hasNext()) {
                    tokens.add(tokenizer.next());
                }
            } catch (Exception e) {
                throw new AnalyzerException("Program includes '%s' that could not be tokenized from '%s': %s"
                        .formatted(referenceNode.value(), includePath, e));
            }

            ProgramNode includedProgramNode;
            try {
                includedProgramNode = ProgramNode.PARSER.parse(tokens);
            } catch (Exception e) {
                throw new AnalyzerException("Program includes '%s' that failed on syntax analysis from '%s': %s"
                        .formatted(referenceNode.value(), includePath, e));
            }

            newClassNodes.addAll(includedProgramNode.classes());
        }

        newClassNodes.addAll(programNode.classes());

        return new ProgramNode(List.of(), newClassNodes);
    }
}
