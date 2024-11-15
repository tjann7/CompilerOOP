package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public final class CompilationUtils {

    public static final String DEFAULT_PACKAGE = "olang";

    private CompilationUtils() {

    }

    @NotNull
    public static String descriptor(@NotNull FieldNode fieldNode) {
        return descriptor(fieldNode.type());
    }

    @NotNull
    public static String descriptor(@NotNull MethodNode methodNode) {
        ParametersNode parameters = methodNode.parameters();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (ParametersNode.Par par : parameters.pars()) {
            stringBuilder.append(descriptor(par.type()));
        }
        stringBuilder.append(")");

        ReferenceNode returnType = methodNode.returnType();
        stringBuilder.append(returnType != null ? descriptor(returnType) : "V");

        return stringBuilder.toString();
    }

    @NotNull
    public static String descriptor(@NotNull IdentifierNode identifierNode) {
        return descriptor(identifierNode.asReference());
    }

    @NotNull
    public static String descriptor(@NotNull ReferenceNode referenceNode) {
        return "L" + DEFAULT_PACKAGE + "." + referenceNode.value() + ";";
    }
}
