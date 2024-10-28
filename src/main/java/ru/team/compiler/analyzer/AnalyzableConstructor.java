package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.ParametersNode;

public record AnalyzableConstructor(@NotNull ParametersNode parameters, @NotNull AnalyzableClass declaredClass) {
}
