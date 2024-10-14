package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.statement.BodyNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class MethodNode extends ClassMemberNode {

    public static final TreeNodeParser<BodyNode> BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);

    public static final TreeNodeParser<MethodNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public MethodNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.METHOD_KEYWORD);

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            ParametersNode parametersNode = ParametersNode.PARSER.parse(iterator);

            IdentifierNode returnIdentifierNode;
            if (iterator.consume(TokenType.COLON)) {
                returnIdentifierNode = IdentifierNode.PARSER.parse(iterator);
            } else {
                returnIdentifierNode = null;
            }

            iterator.next(TokenType.IS_KEYWORD);

            BodyNode bodyNode = BODY_PARSER.parse(iterator);

            iterator.next(TokenType.END_KEYWORD);

            return new MethodNode(identifierNode, parametersNode, returnIdentifierNode, bodyNode);
        }
    };

    private final IdentifierNode name;
    private final ParametersNode parameters;
    private final IdentifierNode returnType;
    private final BodyNode body;

    public MethodNode(@NotNull IdentifierNode name, @NotNull ParametersNode parameters,
                      @Nullable IdentifierNode returnType, @NotNull BodyNode body) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    @NotNull
    public IdentifierNode name() {
        return name;
    }

    @NotNull
    public ParametersNode parameters() {
        return parameters;
    }

    @Nullable
    public IdentifierNode returnType() {
        return returnType;
    }

    @NotNull
    public BodyNode body() {
        return body;
    }
}
