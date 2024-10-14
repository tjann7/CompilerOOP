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
public final class ReturnNode extends StatementNode {

    public static final TreeNodeParser<ReturnNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ReturnNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.RETURN_KEYWORD);

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            return new ReturnNode(expressionNode);
        }
    };

    private final ExpressionNode expression;

    public ReturnNode(@NotNull ExpressionNode expression) {
        this.expression = expression;
    }

    @NotNull
    public ExpressionNode expression() {
        return expression;
    }
}
