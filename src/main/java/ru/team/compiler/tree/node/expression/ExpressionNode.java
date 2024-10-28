package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableConstructor;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.ThisNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ExpressionNode extends TreeNode {

    public static final TreeNodeParser<ExpressionNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ExpressionNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            PrimaryNode primary = PrimaryNode.PARSER.parse(iterator);

            List<IdArg> idArgs = new ArrayList<>();

            if (primary instanceof ReferenceNode && iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
                ArgumentsNode argumentsNode = ArgumentsNode.PARSER.parse(iterator);

                idArgs.add(new IdArg(new IdentifierNode("<init>"), argumentsNode));
            }

            while (iterator.hasNext()) {
                if (iterator.consume(TokenType.DOT)) {
                    IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

                    ArgumentsNode argumentsNode;

                    if (iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
                        argumentsNode = ArgumentsNode.PARSER.parse(iterator);
                    } else {
                        argumentsNode = null;
                    }

                    idArgs.add(new IdArg(identifierNode, argumentsNode));
                } else {
                    break;
                }
            }

            return new ExpressionNode(primary, idArgs);
        }
    };

    private final PrimaryNode primary;
    private final List<IdArg> idArgs;

    public ExpressionNode(@NotNull PrimaryNode primary, @NotNull List<IdArg> idArgs) {
        this.primary = primary;
        this.idArgs = List.copyOf(idArgs);
    }

    @NotNull
    public PrimaryNode primary() {
        return primary;
    }

    @NotNull
    @Unmodifiable
    public List<IdArg> idArgs() {
        return idArgs;
    }

    public record IdArg(@NotNull IdentifierNode name, @Nullable ArgumentsNode arguments) {

    }

    @Override
    @NotNull
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        type(context);
        return context;
    }

    @NotNull
    public ReferenceNode type(@NotNull AnalyzeContext context) {
        ReferenceNode currentType;

        int shift = 0;

        if (primary instanceof ReferenceNode referenceNode) {
            AnalyzableClass analyzableClass = context.classes().get(referenceNode);
            if (analyzableClass != null) {
                if (idArgs.isEmpty()) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to type"
                            .formatted(context.currentPath()));
                }

                IdArg idArg = idArgs.get(0);
                if (!idArg.name.value().equals("<init>")) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to static"
                            .formatted(context.currentPath()));
                }

                if (idArg.arguments == null) {
                    throw new AnalyzerException("Expression in '%s' is invalid: no arguments for constructor"
                            .formatted(context.currentPath()));
                }

                AnalyzableConstructor constructor = findMatchingExecutable(
                        context,
                        analyzableClass,
                        idArg.arguments,
                        currentClass -> new ArrayList<>(currentClass.constructors().values()),
                        AnalyzableConstructor::parameters);

                if (constructor == null) {
                    List<ReferenceNode> argumentsTypes = argumentTypes(context, idArg.arguments);

                    throw new AnalyzerException("Expression in '%s' is invalid: reference to unknown constructor '(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(",")),
                                    referenceNode.value()));
                }

                shift = 1;

                currentType = referenceNode;
            } else {
                if (!idArgs.isEmpty() && idArgs.get(0).name.value().equals("<init>")) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to unknown type '%s'"
                            .formatted(context.currentPath(), referenceNode.value()));
                }

                AnalyzableVariable variable = context.variables().get(referenceNode);
                if (variable != null) {
                    currentType = variable.type();
                } else {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to unknown variable '%s'"
                            .formatted(context.currentPath(), referenceNode.value()));
                }
            }
        } else if (primary instanceof BooleanLiteralNode) {
            currentType = new ReferenceNode("Boolean");
        } else if (primary instanceof IntegerLiteralNode) {
            currentType = new ReferenceNode("Integer");
        } else if (primary instanceof RealLiteralNode) {
            currentType = new ReferenceNode("Real");
        } else if (primary instanceof ThisNode) {
            AnalyzableClass currentClass = context.currentClass("Expression");

            currentType = currentClass.name().asReference();
        } else {
            throw new AnalyzerException("Expression in '%s' is invalid: '%s' is not supported"
                    .formatted(context.currentPath(), primary));
        }

        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            if (idArg.arguments == null) {
                AnalyzableField field = analyzableClass.getField(
                        context,
                        new AnalyzableField.Key(idArg.name),
                        "Expression");

                currentType = field.type();
            } else {
                AnalyzableMethod method = findMatchingExecutable(
                        context,
                        analyzableClass,
                        idArg.arguments,
                        currentClass -> currentClass.methods().values()
                                .stream()
                                .filter(m -> m.name().equals(idArg.name))
                                .collect(Collectors.toList()),
                        AnalyzableMethod::parameters);

                if (method == null) {
                    List<ReferenceNode> argumentsTypes = argumentTypes(context, idArg.arguments);

                    throw new AnalyzerException("Expression in '%s' is invalid: reference to unknown method '%s(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    idArg.name.value(),
                                    argumentsTypes.stream()
                                            .map(ReferenceNode::value)
                                            .collect(Collectors.joining(",")),
                                    currentType.value()));
                }

                if (method.returnType() == null) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to void method '%s(%s)' in type '%s'"
                            .formatted(
                                    context.currentPath(),
                                    idArg.name,
                                    method.parameters().pars().stream()
                                            .map(par -> "? > " + par.type().value())
                                            .collect(Collectors.joining(",")),
                                    currentType.value()));
                }

                currentType = method.returnType();
            }
        }

        return currentType;
    }

    @NotNull
    private List<ReferenceNode> argumentTypes(@NotNull AnalyzeContext context, @NotNull ArgumentsNode arguments) {
        return arguments.expressions()
                .stream()
                .map(expressionNode -> expressionNode.type(context))
                .collect(Collectors.toList());
    }

    @Nullable
    private <E> E findMatchingExecutable(@NotNull AnalyzeContext context, @NotNull AnalyzableClass analyzableClass,
                                         @NotNull ArgumentsNode arguments,
                                         @NotNull Function<AnalyzableClass, List<E>> entitiesFromClass,
                                         @NotNull Function<E, ParametersNode> entityParameters) {
        List<ReferenceNode> argumentsTypes = argumentTypes(context, arguments);

        E finalEntity = null;

        AnalyzableClass currentClass = analyzableClass;
        while (true) {
            List<E> entities = entitiesFromClass.apply(currentClass);

            for (E entity : entities) {
                ParametersNode parameters = entityParameters.apply(entity);
                int size = arguments.expressions().size();
                if (size != parameters.pars().size()) {
                    continue;
                }

                finalEntity = entity;

                for (int j = 0; j < size; j++) {
                    ReferenceNode argumentType = argumentsTypes.get(j);
                    ReferenceNode parameterType = parameters.pars().get(j).type();

                    if (!context.isAssignableFrom(argumentType, parameterType)) {
                        finalEntity = null;
                        break;
                    }
                }

                if (finalEntity != null) {
                    break;
                }
            }

            if (finalEntity != null) {
                break;
            }

            currentClass = currentClass.findParentClass(context, "Expression");
            if (currentClass == null) {
                break;
            }
        }

        return finalEntity;
    }
}
