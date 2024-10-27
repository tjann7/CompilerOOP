package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzeContext;
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

    private final ExpressionNode condition;
    private final BodyNode thenBody;
    private final BodyNode elseBody;

    public IfNode(@NotNull ExpressionNode condition, @NotNull BodyNode thenBody,
                  @Nullable BodyNode elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    @NotNull
    public ExpressionNode condition() {
        return condition;
    }

    @NotNull
    public BodyNode thenBody() {
        return thenBody;
    }

    @Nullable
    public BodyNode elseBody() {
        return elseBody;
    }

    @Override
    @NotNull
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        condition.traverse(context);
        thenBody.traverse(context);
        if (elseBody != null) {
            elseBody.traverse(context);
        }
        return context;
    }
}
