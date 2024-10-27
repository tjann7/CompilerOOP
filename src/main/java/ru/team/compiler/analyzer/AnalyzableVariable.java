package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public record AnalyzableVariable(@NotNull IdentifierNode name, @NotNull ReferenceNode type) {

}
