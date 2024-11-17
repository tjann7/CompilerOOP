package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.SuperNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.StatementNode;
import ru.team.compiler.util.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ConstructorNode extends ClassMemberNode {

    public static final TreeNodeParser<BodyNode> BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);

    public static final TreeNodeParser<ConstructorNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ConstructorNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.THIS_KEYWORD);

            boolean isNative = iterator.consume(TokenType.NATIVE_KEYWORD);

            ParametersNode parametersNode = ParametersNode.PARSER.parse(iterator);

            BodyNode bodyNode;
            if (!isNative) {
                iterator.next(TokenType.IS_KEYWORD);

                bodyNode = BODY_PARSER.parse(iterator);

                iterator.next(TokenType.END_KEYWORD);
            } else {
                bodyNode = new BodyNode(List.of());

                iterator.next(TokenType.SEMICOLON);
            }

            boolean createSuperCall = !hasSuperCall(bodyNode);

            if (createSuperCall) {
                List<StatementNode> statements = bodyNode.statements();
                List<StatementNode> newStatements = new ArrayList<>(statements.size() + 1);
                newStatements.add(
                        new MethodCallNode(
                                new ExpressionNode(
                                        new SuperNode(), List.of(
                                        new ExpressionNode.IdArg(
                                                new IdentifierNode("<init>"),
                                                new ArgumentsNode(List.of()))))));
                newStatements.addAll(statements);
                bodyNode = new BodyNode(newStatements);
            }

            return new ConstructorNode(isNative, parametersNode, bodyNode, createSuperCall);
        }
    };

    private final boolean isNative;
    private final ParametersNode parameters;
    private final BodyNode body;
    private final boolean syntheticSuperCall;

    public ConstructorNode(boolean isNative, @NotNull ParametersNode parameters, @NotNull BodyNode body,
                           boolean syntheticSuperCall) {
        this.isNative = isNative;
        this.parameters = parameters;
        this.body = body;
        this.syntheticSuperCall = syntheticSuperCall;
    }

    public boolean isNative() {
        return isNative;
    }

    @NotNull
    public ParametersNode parameters() {
        return parameters;
    }

    @NotNull
    public BodyNode body() {
        return body;
    }

    public boolean syntheticSuperCall() {
        return syntheticSuperCall;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        context = context.withConstructor(this);
        context = parameters.analyze(context);
        context = body.analyze(context);

        if (!hasCorrectSuperCall(body)) {
            if (hasSuperCall(body)) {
                throw new AnalyzerException("Constructor at '%s' is invalid: super constructor call must be the first operation"
                        .formatted(context.currentPath()));
            } else {
                throw new AnalyzerException("Constructor at '%s' is invalid: does not have super constructor call"
                        .formatted(context.currentPath()));
            }
        }

        List<StatementNode> flatStatements = body.flatStatements();
        for (StatementNode statementNode : flatStatements.subList(1, flatStatements.size())) {
            if (isSuperCall(statementNode)) {
                throw new AnalyzerException("Constructor at '%s' is invalid: more than one super constructor call"
                        .formatted(context.currentPath()));
            }
        }

        AnalyzableClass analyzableClass = context.currentClass("Constructor");

        Set<ReferenceNode> allFields = analyzableClass.fields().values()
                .stream()
                .map(AnalyzableField::name)
                .map(IdentifierNode::asReference)
                .collect(Collectors.toSet());

        if (!allFields.equals(context.initializedFields())) {
            String message;
            Set<ReferenceNode> difference = Sets.difference(allFields, context.initializedFields());
            if (difference.isEmpty()) {
                difference = Sets.difference(context.initializedFields(), allFields);
                message = "define unknown field";
            } else {
                message = "does not define field";
            }

            if (!difference.isEmpty()) {
                throw new AnalyzerException("Constructor '%s(%s)' %s%s %s"
                        .formatted(
                                analyzableClass.name().value(),
                                parameters.pars().stream()
                                        .map(ParametersNode.Par::type)
                                        .map(ReferenceNode::value)
                                        .collect(Collectors.joining(",")),
                                message,
                                difference.size() == 1 ? "" : "s",
                                difference.stream()
                                        .map(type -> "this." + type.value())
                                        .collect(Collectors.joining(","))));
            }

            difference = Sets.difference(context.initializedFields(), allFields);
        }

        return initialContext;
    }

    @Override
    @NotNull
    public ConstructorNode optimize() {
        return new ConstructorNode(
                isNative,
                parameters,
                body.optimize(),
                syntheticSuperCall
        );
    }

    private static boolean hasSuperCall(@NotNull BodyNode bodyNode) {
        List<StatementNode> statements = bodyNode.statements();
        for (StatementNode statementNode : statements) {
            if (isSuperCall(statementNode)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasCorrectSuperCall(@NotNull BodyNode bodyNode) {
        List<StatementNode> statements = bodyNode.statements();
        return !statements.isEmpty() && isSuperCall(statements.get(0));
    }

    private static boolean isSuperCall(@NotNull StatementNode statementNode) {
        if (statementNode instanceof MethodCallNode methodCallNode) {
            ExpressionNode expression = methodCallNode.expression();
            PrimaryNode primary = expression.primary();
            List<ExpressionNode.IdArg> idArgs = expression.idArgs();

            return primary instanceof SuperNode && !idArgs.isEmpty() && idArgs.get(0).name().value().equals("<init>");
        }

        return false;
    }

}
