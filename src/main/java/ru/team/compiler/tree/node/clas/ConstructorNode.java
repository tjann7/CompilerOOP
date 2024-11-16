package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.statement.BodyNode;

import java.util.List;

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

        context = context.concatPath("this");
        context = parameters.analyze(context);
        body.analyze(context);

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
