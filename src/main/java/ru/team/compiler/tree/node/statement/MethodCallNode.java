package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class MethodCallNode extends StatementNode {

    public static final TreeNodeParser<MethodCallNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public MethodCallNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            ExpressionNode expression = ExpressionNode.PARSER.parse(iterator);
            return new MethodCallNode(expression);
        }
    };

    private final ExpressionNode expression;

    public MethodCallNode(@NotNull ExpressionNode expression) {
        this.expression = expression;
    }

    @NotNull
    public ExpressionNode expression() {
        return expression;
    }
}
