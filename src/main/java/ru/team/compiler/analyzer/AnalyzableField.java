package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public record AnalyzableField(@NotNull IdentifierNode name, @NotNull ReferenceNode type,
                              @NotNull AnalyzableClass declaredClass) {

    @NotNull
    public Key key() {
        return new Key(name);
    }

    public record Key(@NotNull IdentifierNode name) {

    }
}
