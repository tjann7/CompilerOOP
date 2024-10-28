package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Collections;
import java.util.Map;

public record AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                             @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                             @NotNull String currentPath,
                             @Nullable AnalyzableClass currentClass) {

    public AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                          @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                          @NotNull String currentPath,
                          @Nullable AnalyzableClass currentClass) {
        this.classes = Collections.unmodifiableMap(classes);
        this.variables = Collections.unmodifiableMap(variables);
        this.currentPath = currentPath;
        this.currentClass = currentClass;
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
                classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path, currentClass
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
                classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path, analyzableClass
        );
    }
}
