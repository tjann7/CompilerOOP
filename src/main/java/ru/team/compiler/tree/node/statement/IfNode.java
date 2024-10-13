package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class IfNode extends StatementNode {

    public static final TreeNodeParser<BodyNode> THEN_BODY_PARSER = BodyNode.parser(
            TokenType.ELSE_KEYWORD, TokenType.END_KEYWORD
    );
    public static final TreeNodeParser<BodyNode> ELSE_BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);

    public static final TreeNodeParser<IfNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public IfNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.IF_KEYWORD);

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            iterator.next(TokenType.THEN_KEYWORD);

            BodyNode thenBodyNode = THEN_BODY_PARSER.parse(iterator);

            BodyNode elseBodyNode;

            if (iterator.consume(TokenType.ELSE_KEYWORD)) {
                elseBodyNode = ELSE_BODY_PARSER.parse(iterator);
            } else {
                elseBodyNode = new BodyNode(List.of());
            }

            iterator.next(TokenType.END_KEYWORD);

            return new IfNode(expressionNode, thenBodyNode, elseBodyNode);
        }
    };

    private final ExpressionNode expressionNode;
    private final BodyNode thenBodyNode;
    private final BodyNode elseBodyNode;

    public IfNode(@NotNull ExpressionNode expressionNode, @NotNull BodyNode thenBodyNode, @Nullable BodyNode elseBodyNode) {
        this.expressionNode = expressionNode;
        this.thenBodyNode = thenBodyNode;
        this.elseBodyNode = elseBodyNode;
    }

    @NotNull
    public ExpressionNode expressionNode() {
        return expressionNode;
    }

    @NotNull
    public BodyNode thenBodyNode() {
        return thenBodyNode;
    }

    @Nullable
    public BodyNode elseBodyNode() {
        return elseBodyNode;
    }
}
