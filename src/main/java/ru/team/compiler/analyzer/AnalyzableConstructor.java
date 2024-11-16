package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record AnalyzableConstructor(@NotNull ConstructorNode constructorNode, @NotNull ParametersNode parameters,
                                    @NotNull AnalyzableClass declaredClass) {

    @NotNull
    public Key key() {
        List<ReferenceNode> parameterTypes = parameters.pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList());

        return new Key(parameterTypes);
    }

    @Override
    public String toString() {
        return "AnalyzableConstructor{" +
                "parameters=" + parameters +
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
        AnalyzableConstructor that = (AnalyzableConstructor) object;
        return Objects.equals(parameters, that.parameters)
                && Objects.equals(declaredClass.name(), that.declaredClass.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, declaredClass.name());
    }

    public record Key(@NotNull List<ReferenceNode> parameterTypes) {

    }
}
