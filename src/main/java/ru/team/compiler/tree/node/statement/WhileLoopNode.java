package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class WhileLoopNode extends StatementNode {

    public static final TreeNodeParser<BodyNode> BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);
    public static final TreeNodeParser<WhileLoopNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public WhileLoopNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.WHILE_KEYWORD);

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            iterator.next(TokenType.LOOP_KEYWORD);

            BodyNode bodyNode = BODY_PARSER.parse(iterator);

            iterator.next(TokenType.END_KEYWORD);

            return new WhileLoopNode(expressionNode, bodyNode);
        }
    };

    private final ExpressionNode expressionNode;
    private final BodyNode bodyNode;

    public WhileLoopNode(@NotNull ExpressionNode expressionNode, @NotNull BodyNode bodyNode) {
        this.expressionNode = expressionNode;
        this.bodyNode = bodyNode;
    }

    @NotNull
    public ExpressionNode expressionNode() {
        return expressionNode;
    }

    @NotNull
    public BodyNode bodyNode() {
        return bodyNode;
    }
}
