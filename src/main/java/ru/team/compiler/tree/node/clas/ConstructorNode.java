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
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.util.Sets;

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
            }

            return new ConstructorNode(isNative, parametersNode, bodyNode);
        }
    };

    private final boolean isNative;
    private final ParametersNode parameters;
    private final BodyNode body;

    public ConstructorNode(boolean isNative, @NotNull ParametersNode parameters, @NotNull BodyNode body) {
        this.isNative = isNative;
        this.parameters = parameters;
        this.body = body;
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

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        context = context.withConstructor(this);
        context = parameters.analyze(context);
        context = body.analyze(context);

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
                body.optimize()
        );
    }

}
