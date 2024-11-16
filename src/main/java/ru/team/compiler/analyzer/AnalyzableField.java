package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public record AnalyzableField(@NotNull FieldNode fieldNode, @NotNull IdentifierNode name, @NotNull ReferenceNode type,
                              @NotNull AnalyzableClass declaredClass) {

    @NotNull
    public Key key() {
        return new Key(name);
    }

    @Override
    public String toString() {
        return "AnalyzableField{" +
                "name=" + name +
                ", type=" + type +
                ", declaredClass=" + declaredClass.name() +
                '}';
    }

    public record Key(@NotNull IdentifierNode name) {

    }
}
