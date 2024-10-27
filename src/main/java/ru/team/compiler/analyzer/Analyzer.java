package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.ClassMemberNode;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.clas.ProgramNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.HashMap;
import java.util.Map;

public final class Analyzer {

    private Analyzer() {

    }

    @NotNull
    private static Map<ReferenceNode, AnalyzableClass> stdClasses() {
        Map<ReferenceNode, AnalyzableClass> classes = new HashMap<>();

        classes.put(
                new ReferenceNode("Any"),
                new AnalyzableClass(new IdentifierNode("Any"), new ReferenceNode(""), Map.of(), Map.of())
        );
        classes.put(
                new ReferenceNode("Integer"),
                new AnalyzableClass(new IdentifierNode("Integer"), new ReferenceNode("Any"), Map.of(), Map.of())
        );
        classes.put(
                new ReferenceNode("Real"),
                new AnalyzableClass(new IdentifierNode("Real"), new ReferenceNode("Any"), Map.of(), Map.of())
        );
        classes.put(
                new ReferenceNode("Boolean"),
                new AnalyzableClass(new IdentifierNode("Boolean"), new ReferenceNode("Any"), Map.of(), Map.of())
        );

        return classes;
    }

    public static void traverse(@NotNull ProgramNode programNode) {
        Map<ReferenceNode, AnalyzableClass> classes = new HashMap<>(stdClasses());

        for (ClassNode classNode : programNode.classes()) {
            ReferenceNode classReference = classNode.name().asReference();
            if (classes.containsKey(classReference)) {
                throw new AnalyzerException("Class '%s' is already defined".formatted(classNode.name()));
            }

            Map<AnalyzableMethod.Key, AnalyzableMethod> methods = new HashMap<>();
            Map<AnalyzableField.Key, AnalyzableField> fields = new HashMap<>();

            ReferenceNode parentName = classNode.parentName();
            AnalyzableClass analyzableClass = new AnalyzableClass(
                    classNode.name(),
                    parentName != null ? parentName : new ReferenceNode("Any"),
                    methods,
                    fields
            );

            for (ClassMemberNode classMemberNode : classNode.classMembers()) {

                if (classMemberNode instanceof MethodNode methodNode) {
                    IdentifierNode name = methodNode.name();
                    ParametersNode parameters = methodNode.parameters();
                    ReferenceNode type = methodNode.returnType();

                    AnalyzableMethod method = new AnalyzableMethod(name, parameters, type, analyzableClass);
                    methods.put(method.key(), method);
                } else if (classMemberNode instanceof FieldNode fieldNode) {
                    IdentifierNode name = fieldNode.name();
                    ReferenceNode type = fieldNode.type();

                    AnalyzableField field = new AnalyzableField(name, type, analyzableClass);
                    fields.put(field.key(), field);
                }
            }

            classes.put(classReference, analyzableClass);
        }

        programNode.traverse(new AnalyzeContext(classes, Map.of(), ""));
    }

}
