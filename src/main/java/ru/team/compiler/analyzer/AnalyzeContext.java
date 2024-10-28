package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public record AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                             @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                             @NotNull String currentPath,
                             @Nullable AnalyzableClass currentClass,
                             @Nullable AnalyzableMethod currentMethod) {

    public AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                          @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                          @NotNull String currentPath,
                          @Nullable AnalyzableClass currentClass,
                          @Nullable AnalyzableMethod currentMethod) {
        this.classes = Collections.unmodifiableMap(classes);
        this.variables = Collections.unmodifiableMap(variables);
        this.currentPath = currentPath;
        this.currentClass = currentClass;
        this.currentMethod = currentMethod;
    }

    public boolean hasClass(@NotNull ReferenceNode className) {
        return classes.containsKey(className);
    }

    public boolean hasVariable(@NotNull ReferenceNode variableName) {
        return variables.containsKey(variableName);
    }

    @NotNull
    public AnalyzeContext concatPath(@NotNull String path) {
        return new AnalyzeContext(
                classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path, currentClass, currentMethod
        );
    }

    @NotNull
    public AnalyzeContext withClass(@NotNull ClassNode classNode) {
        AnalyzableClass analyzableClass = classes.get(classNode.name().asReference());
        if (analyzableClass == null) {
            throw new AnalyzerException("Reference to unknown class '%s'".formatted(classNode.name().value()));
        }

        String path = classNode.name().value();
        return new AnalyzeContext(
                classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path, analyzableClass, currentMethod
        );
    }

    @NotNull
    public AnalyzeContext withMethod(@NotNull MethodNode method) {
        AnalyzableMethod.Key key = new AnalyzableMethod.Key(method.name(), method.parameters().pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList()));

        if (currentClass == null) {
            throw new AnalyzerException("'%s' is invalid: outside of the class context"
                    .formatted(currentPath()));
        }

        AnalyzableMethod analyzableMethod = currentClass.methods().get(key);
        if (analyzableMethod == null) {
            throw new AnalyzerException("Reference to unknown method '%s.%s(%s)'"
                    .formatted(currentClass.name().value(), method.name().value(), key.parameterTypes().stream()
                            .map(ReferenceNode::value)
                            .collect(Collectors.joining(","))));
        }

        String path = analyzableMethod.name().value();
        return new AnalyzeContext(
                classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path, currentClass, analyzableMethod
        );
    }

    public boolean isAssignableFrom(@NotNull ReferenceNode requiredClassName, @NotNull ReferenceNode className) {
        if (!hasClass(requiredClassName)) {
            throw new AnalyzerException("Class '%s' cannot be found at '%s'"
                    .formatted(requiredClassName.value(), currentPath));
        } else if (!hasClass(className)) {
            throw new AnalyzerException("Class '%s' cannot be found at '%s'"
                    .formatted(className.value(), currentPath));
        }

        return classes.get(requiredClassName).isAssignableFrom(this, classes.get(className));
    }

    @NotNull
    public AnalyzableClass currentClass(@NotNull String messagePrefix) {
        if (currentClass == null) {
            throw new AnalyzerException("%s at '%s' is invalid: outside of the class context"
                    .formatted(messagePrefix, currentPath()));
        }

        return currentClass;
    }

    @NotNull
    public AnalyzableMethod currentMethod(@NotNull String messagePrefix) {
        if (currentMethod == null) {
            throw new AnalyzerException("%s at '%s' is invalid: outside of the method context"
                    .formatted(messagePrefix, currentPath()));
        }

        return currentMethod;
    }
}
