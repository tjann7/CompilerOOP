package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;
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

    public record Key(@NotNull List<ReferenceNode> parameterTypes) {

    }
}
