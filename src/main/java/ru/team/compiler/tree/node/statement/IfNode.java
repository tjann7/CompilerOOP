package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
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
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        ReferenceNode type = condition.type(context);

        if (!type.value().equals("Boolean")) {
            throw new AnalyzerException("If condition at '%s' is invalid: expected 'Boolean' type, got '%s'"
                    .formatted(context.currentPath(), type.value()));
        }

        thenBody.analyze(context);
        if (elseBody != null) {
            elseBody.analyze(context);
        }
        return context;
    }

    @Override
    public boolean alwaysReturn() {
        return thenBody.alwaysReturn() && (elseBody != null && elseBody().alwaysReturn());
    }

    @Override
    @NotNull
    public List<StatementNode> optimize() {
        if (condition.primary() instanceof BooleanLiteralNode booleanLiteralNode && condition.idArgs().isEmpty()) {
            if (booleanLiteralNode.value()) {
                return thenBody.optimize().statements();
            } else {
                return elseBody != null ? elseBody.optimize().statements() : List.of();
            }
        }

        BodyNode optimizedThenBody = thenBody.optimize();
        BodyNode optimizedElseBody = elseBody != null ? elseBody.optimize() : null;

        if (optimizedThenBody.statements().isEmpty()) {
            optimizedThenBody = null;
        }

        if (optimizedElseBody != null && optimizedElseBody.statements().isEmpty()) {
            optimizedElseBody = null;
        }

        if (optimizedThenBody == null) {
            if (optimizedElseBody == null) {
                MethodCallNode methodCall = condition.asMethodCall();
                return methodCall != null ? List.of(methodCall) : List.of();
            } else {
                return List.of(new IfNode(
                        condition.withIdArgs(List.of(
                                new ExpressionNode.IdArg(
                                        new IdentifierNode("not"), new ArgumentsNode(List.of())))),
                        optimizedElseBody,
                        null));
            }
        }

        if (thenBody.equals(elseBody)) {
            MethodCallNode methodCall = condition.asMethodCall();
            if (methodCall != null) {
                List<StatementNode> optimized = new ArrayList<>(1 + thenBody.statements().size());

                optimized.add(methodCall);
                optimized.addAll(thenBody.statements());

                return optimized;
            } else {
                return thenBody.statements();
            }
        }

        return List.of(new IfNode(condition, optimizedThenBody, optimizedElseBody));
    }
}
