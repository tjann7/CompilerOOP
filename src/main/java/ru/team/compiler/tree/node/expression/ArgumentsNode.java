package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ArgumentsNode extends TreeNode {

    public static final TreeNodeParser<ArgumentsNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ArgumentsNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.OPENING_PARENTHESIS);

            List<ExpressionNode> expressionNodes = new ArrayList<>();

            if (!iterator.consume(TokenType.CLOSING_PARENTHESIS)) {
                while (true) {
                    ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);
                    expressionNodes.add(expressionNode);

                    if (iterator.consume(TokenType.CLOSING_PARENTHESIS)) {
                        break;
                    } else if (!iterator.consume(TokenType.COMMA)) {
                        throw new NodeFormatException("comma/closing parenthesis", NodeFormatException.END_OF_STRING,
                                iterator.lastToken());
                    }
                }
            }

            return new ArgumentsNode(expressionNodes);
        }
    };

    private final List<ExpressionNode> expressions;

    public ArgumentsNode(@NotNull List<ExpressionNode> expressions) {
        this.expressions = List.copyOf(expressions);
    }

    @NotNull
    @Unmodifiable
    public List<ExpressionNode> expressions() {
        return expressions;
    }

    @Override
    @NotNull
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;
        context = context.concatPath("<arguments>");
        for (ExpressionNode expressionNode : expressions) {
            context = expressionNode.traverse(context);
        }

        return initialContext;
    }
}
