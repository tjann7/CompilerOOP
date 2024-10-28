package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.*;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.*;

public final class Analyzer {

    private Analyzer() {

    }

    public static void traverse(@NotNull ProgramNode programNode) {
        List<ClassNode> classes = programNode.classes();

        Map<String, AnalyzableClass> classMap = new HashMap<>();

        for (ClassNode classNode : classes) {
            List<ClassMemberNode> classMemberNodes = classNode.classMembers();
            Map<AnalyzableField, String> fields = new HashMap<>();
            Map<AnalyzableMethod, String> methods = new HashMap<>();
            Map<AnalyzableConstructor, String> constructors = new HashMap<>();
            for (ClassMemberNode classMemberNode : classMemberNodes) {
                if (classMemberNode instanceof MethodNode methodNode) {
                    ReferenceNode type = methodNode.returnType();
                    AnalyzableMethod method = new AnalyzableMethod(methodNode.name(), methodNode.parameters());
                    if (methods.containsKey(method)) {
                        throw new AnalyzerException();
                    }
                    methods.put(method, type.value());
                } else if (classMemberNode instanceof FieldNode fieldNode) {
                    ReferenceNode type = fieldNode.type();
                    AnalyzableField field = new AnalyzableField(fieldNode.name());
                    if (fields.containsKey(field)) {
                        throw new AnalyzerException();
                    }
                    fields.put(field, type.value());
                } else if (classMemberNode instanceof ConstructorNode constructorNode) {
                    AnalyzableConstructor constructor = new AnalyzableConstructor(constructorNode.parameters());
                    if (constructors.containsKey(constructor)) {
                        throw new AnalyzerException();
                    }
                    constructors.put(constructor, "this");
                }
            }
            AnalyzableClass analyzableClass = new AnalyzableClass(classNode.name().value(),
                    methods, fields, constructors);
            classMap.put(analyzableClass.name(), analyzableClass);

            // 2

            programNode.traverse(new AnalyzeContext(classMap, Map.of()));
        }

    }
}
