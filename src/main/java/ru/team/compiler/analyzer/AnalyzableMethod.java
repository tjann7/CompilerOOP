package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;
import java.util.stream.Collectors;

public record AnalyzableMethod(@NotNull MethodNode methodNode, @NotNull IdentifierNode name,
                               @NotNull ParametersNode parameters, @Nullable ReferenceNode returnType,
                               @NotNull AnalyzableClass declaredClass) {

    @NotNull
    public Key key() {
        List<ReferenceNode> parameterTypes = parameters.pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList());

        return new Key(name, parameterTypes);
    }

    @Override
    public String toString() {
        return "AnalyzableMethod{" +
                "name=" + name +
                ", parameters=" + parameters +
                ", returnType=" + returnType +
                ", declaredClass=" + declaredClass.name() +
                '}';
    }

    public record Key(@NotNull IdentifierNode name, @NotNull List<ReferenceNode> parameterTypes) {

    }
}
