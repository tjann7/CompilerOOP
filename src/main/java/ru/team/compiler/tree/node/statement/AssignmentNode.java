package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class AssignmentNode extends StatementNode {

    public static final TreeNodeParser<AssignmentNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public AssignmentNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            TokenIterator copiedIterator = iterator.copy();

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            List<ExpressionNode.IdArg> idArgs = expressionNode.idArgs();
            if (!idArgs.isEmpty() && idArgs.get(idArgs.size() - 1).arguments() != null) {
                int startIndex = copiedIterator.index();
                int endIndex = iterator.index();

                StringBuilder stringBuilder = new StringBuilder();
                Token lastToken = null;
                for (int i = startIndex; i < endIndex; i++) {
                    lastToken = copiedIterator.next();
                    stringBuilder.append(lastToken.value());
                }

                throw new NodeFormatException("field", "method call at " + stringBuilder, lastToken);
            }

            iterator.next(TokenType.ASSIGNMENT_OPERATOR);

            ExpressionNode valueExpressionNode = ExpressionNode.PARSER.parse(iterator);
            return new AssignmentNode(expressionNode, valueExpressionNode);
        }
    };

    private final ExpressionNode expression;
    private final ExpressionNode valueExpression;

    public AssignmentNode(@NotNull ExpressionNode expression, @NotNull ExpressionNode valueExpression) {
        this.expression = expression;
        this.valueExpression = valueExpression;
    }

    @NotNull
    public ExpressionNode expression() {
        return expression;
    }

    @NotNull
    public ExpressionNode valueExpression() {
        return valueExpression;
    }
}
