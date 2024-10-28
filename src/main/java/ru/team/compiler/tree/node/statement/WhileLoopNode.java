package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;

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

    private final ExpressionNode condition;
    private final BodyNode body;

    public WhileLoopNode(@NotNull ExpressionNode condition, @NotNull BodyNode body) {
        this.condition = condition;
        this.body = body;
    }

    @NotNull
    public ExpressionNode condition() {
        return condition;
    }

    @NotNull
    public BodyNode body() {
        return body;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        ReferenceNode type = condition.type(context);

        if (!type.value().equals("Boolean")) {
            throw new AnalyzerException("While condition at '%s' is invalid: expected 'Boolean' type, got '%s'"
                    .formatted(context.currentPath(), type.value()));
        }

        body.analyze(context);
        return context;
    }

    @Override
    @NotNull
    public List<StatementNode> optimize() {
        if (condition.primary() instanceof BooleanLiteralNode booleanLiteralNode
                && condition.idArgs().isEmpty()
                && !booleanLiteralNode.value()) {
            return List.of();
        }

        return List.of(new WhileLoopNode(condition, body.optimize()));
    }
}
