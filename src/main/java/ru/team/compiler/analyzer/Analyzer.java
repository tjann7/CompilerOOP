package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Analyzer {

    private Analyzer() {

    }

    @NotNull
    private static Map<ReferenceNode, AnalyzableClass> loadClasses(@NotNull InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (reader.ready()) {
                stringBuilder.append(reader.readLine()).append("\n");
            }
        }

        Tokenizer tokenizer = new Tokenizer(stringBuilder.toString());
        List<Token> tokens = new ArrayList<>();
        while (tokenizer.hasNext()) {
            tokens.add(tokenizer.next());
        }

        ProgramNode programNode = ProgramNode.PARSER.parse(tokens);
        AnalyzeContext context = createContext(programNode, Map.of());
        return context.classes();
    }

    @NotNull
    private static Map<ReferenceNode, AnalyzableClass> stdClasses() {
        InputStream inputStream = Analyzer.class.getClassLoader().getResourceAsStream("std.o");
        if (inputStream == null) {
            throw new AnalyzerException("Cannot read standard library: file not found");
        }

        try {
            return loadClasses(inputStream);
        } catch (IOException e) {
            throw new AnalyzerException("Cannot read standard library", e);
        }
    }

    @Nullable
    private static String hasCyclicDependency(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                                              @NotNull ReferenceNode current, @NotNull ReferenceNode origin,
                                              @Nullable List<String> path) {
        if (path == null) {
            path = new ArrayList<>();
            path.add(origin.value());
        }

        AnalyzableClass analyzableClass = classes.get(current);
        if (analyzableClass == null) {
            return null;
        }

        ReferenceNode parent = analyzableClass.parentClass();
        path.add(parent.value());

        if (parent.equals(origin)) {
            return String.join(" -> ", path);
        }

        return hasCyclicDependency(classes, parent, origin, path);
    }

    @NotNull
    public static AnalyzeContext createContext(@NotNull ProgramNode programNode) {
        return createContext(programNode, stdClasses());
    }

    @NotNull
    private static AnalyzeContext createContext(@NotNull ProgramNode programNode,
                                                @NotNull Map<ReferenceNode, AnalyzableClass> parentClasses) {
        Map<ReferenceNode, AnalyzableClass> classes = new HashMap<>(parentClasses);

        for (ClassNode classNode : programNode.classes()) {
            ReferenceNode classReference = classNode.name().asReference();
            if (classes.containsKey(classReference)) {
                throw new AnalyzerException("Class '%s' is already defined"
                        .formatted(classNode.name().value()));
            }

            Map<AnalyzableConstructor.Key, AnalyzableConstructor> constructors = new HashMap<>();
            Map<AnalyzableMethod.Key, AnalyzableMethod> methods = new HashMap<>();
            Map<AnalyzableField.Key, AnalyzableField> fields = new HashMap<>();

            ReferenceNode parentName = classNode.parentName();
            AnalyzableClass analyzableClass = new AnalyzableClass(
                    classNode.name(),
                    parentName != null ? parentName : new ReferenceNode("Any"),
                    constructors,
                    methods,
                    fields
            );

            for (ClassMemberNode classMemberNode : classNode.classMembers()) {

                if (classMemberNode instanceof MethodNode methodNode) {
                    IdentifierNode name = methodNode.name();
                    ParametersNode parameters = methodNode.parameters();
                    ReferenceNode type = methodNode.returnType();

                    AnalyzableMethod method = new AnalyzableMethod(name, parameters, type, analyzableClass);
                    AnalyzableMethod.Key key = method.key();

                    if (methods.containsKey(key)) {
                        throw new AnalyzerException("Method '%s.%s(%s)' is already defined"
                                .formatted(
                                        classNode.name().value(),
                                        name.value(),
                                        key.parameterTypes().stream()
                                                .map(ReferenceNode::value)
                                                .collect(Collectors.joining(","))));
                    }

                    methods.put(key, method);
                } else if (classMemberNode instanceof FieldNode fieldNode) {
                    IdentifierNode name = fieldNode.name();
                    ReferenceNode type = fieldNode.type();

                    AnalyzableField field = new AnalyzableField(name, type, analyzableClass);

                    if (fields.containsKey(field.key())) {
                        throw new AnalyzerException("Field '%s.%s' is already defined"
                                .formatted(classNode.name().value(), name.value()));
                    }

                    fields.put(field.key(), field);
                } else if (classMemberNode instanceof ConstructorNode constructorNode) {
                    ParametersNode parameters = constructorNode.parameters();

                    AnalyzableConstructor constructor = new AnalyzableConstructor(parameters, analyzableClass);
                    AnalyzableConstructor.Key key = constructor.key();
                    if (constructors.containsKey(key)) {
                        throw new AnalyzerException("Constructor '%s(%s)' is already defined"
                                .formatted(
                                        classNode.name().value(),
                                        key.parameterTypes().stream()
                                                .map(ReferenceNode::value)
                                                .collect(Collectors.joining(","))));
                    }
                    constructors.put(key, constructor);
                }
            }

            classes.put(classReference, analyzableClass);

            String path = hasCyclicDependency(classes, classReference, classReference, null);
            if (path != null) {
                throw new AnalyzerException("Class '%s' has cyclic dependency: %s"
                        .formatted(classNode.name().value(), path));
            }

        }

        return new AnalyzeContext(classes, Map.of(), "", null, null);
    }

}
