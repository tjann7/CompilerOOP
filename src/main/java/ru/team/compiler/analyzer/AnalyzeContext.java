package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Collections;
import java.util.Map;

public record AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                             @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                             @NotNull String currentPath) {

    public AnalyzeContext(@NotNull Map<ReferenceNode, AnalyzableClass> classes,
                          @NotNull Map<ReferenceNode, AnalyzableVariable> variables,
                          @NotNull String currentPath) {
        this.classes = Collections.unmodifiableMap(classes);
        this.variables = Collections.unmodifiableMap(variables);
        this.currentPath = currentPath;
    }

    public boolean hasClass(@NotNull ReferenceNode className) {
        return classes.containsKey(className);
    }

    @NotNull
    public AnalyzeContext concatPath(@NotNull String path) {
        return new AnalyzeContext(classes, variables, currentPath.isEmpty() ? path : currentPath + "." + path);
    }
}
