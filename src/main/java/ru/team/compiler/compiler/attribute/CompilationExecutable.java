package ru.team.compiler.compiler.attribute;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzableConstructor;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;

import java.util.stream.Collectors;

public record CompilationExecutable(@NotNull ClassNode classNode, @NotNull IdentifierNode name,
                                    @NotNull ParametersNode parametersNode) {

    public boolean isConstructor() {
        return name.value().equals("<init>");
    }

    @NotNull
    public AnalyzableMethod.Key asMethodKey() {
        return new AnalyzableMethod.Key(name, parametersNode.pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList()));
    }

    @NotNull
    public AnalyzableConstructor.Key asConstructorKey() {
        return new AnalyzableConstructor.Key(parametersNode.pars().stream()
                .map(ParametersNode.Par::type)
                .collect(Collectors.toList()));
    }
}
