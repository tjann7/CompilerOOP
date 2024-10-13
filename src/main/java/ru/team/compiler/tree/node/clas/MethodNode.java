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

    private final IdentifierNode identifierNode;
    private final ParametersNode parametersNode;
    private final IdentifierNode returnIdentifierNode;
    private final BodyNode bodyNode;

    public MethodNode(@NotNull IdentifierNode identifierNode, @NotNull ParametersNode parametersNode,
                      @Nullable IdentifierNode returnIdentifierNode, @NotNull BodyNode bodyNode) {
        this.identifierNode = identifierNode;
        this.parametersNode = parametersNode;
        this.returnIdentifierNode = returnIdentifierNode;
        this.bodyNode = bodyNode;
    }

    @NotNull
    public IdentifierNode identifierNode() {
        return identifierNode;
    }

    @NotNull
    public ParametersNode parametersNode() {
        return parametersNode;
    }

    @Nullable
    public IdentifierNode returnIdentifierNode() {
        return returnIdentifierNode;
    }

    @NotNull
    public BodyNode bodyNode() {
        return bodyNode;
    }
}
