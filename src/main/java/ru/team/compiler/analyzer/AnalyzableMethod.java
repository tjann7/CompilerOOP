package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public record AnalyzableMethod(@NotNull IdentifierNode name, @NotNull ParametersNode parameters,
                               @Nullable ReferenceNode returnType, @NotNull AnalyzableClass declaredClass) {

    @NotNull
    public Key key() {
        return new Key(name, parameters);
    }

    public record Key(@NotNull IdentifierNode name, @NotNull ParametersNode parameters) {

    }
}
