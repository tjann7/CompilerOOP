package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AnalyzableField that = (AnalyzableField) object;
        return Objects.equals(name, that.name)
                && Objects.equals(declaredClass.name(), that.declaredClass.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaredClass.name());
    }

    public record Key(@NotNull IdentifierNode name) {

    }
}
