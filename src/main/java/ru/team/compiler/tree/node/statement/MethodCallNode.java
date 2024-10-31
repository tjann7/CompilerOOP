package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class MethodCallNode extends StatementNode {

    public static final TreeNodeParser<MethodCallNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public MethodCallNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            Token token = iterator.lookup();

            ExpressionNode expression = ExpressionNode.PARSER.parse(iterator);

            List<ExpressionNode.IdArg> idArgs = expression.idArgs();
            if (idArgs.isEmpty() || idArgs.get(idArgs.size() - 1).arguments() == null) {
                throw new NodeFormatException("method call", "field reference", token);
            }

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

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        expression.type(context, true);
        return context;
    }
}
