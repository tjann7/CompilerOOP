package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ReturnNode extends StatementNode {

    public static final TreeNodeParser<ReturnNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ReturnNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.RETURN_KEYWORD);

            if (iterator.lookup(TokenType.END_KEYWORD) || iterator.lookup(TokenType.SEMICOLON)) {
                return new ReturnNode(null);
            }

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            return new ReturnNode(expressionNode);
        }
    };

    private final ExpressionNode expression;

    public ReturnNode(@Nullable ExpressionNode expression) {
        this.expression = expression;
    }

    @Nullable
    public ExpressionNode expression() {
        return expression;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        ReferenceNode type = expression != null ? expression.type(context) : null;

        AnalyzableMethod currentMethod = context.currentMethod("Return");
        if (!Objects.equals(type, currentMethod.returnType())) {
            throw new AnalyzerException("Return at '%s' is invalid: expected '%s', got '%s'"
                    .formatted(context.currentPath(), prettyType(currentMethod.returnType()), prettyType(type)));
        }

        return context;
    }

    @NotNull
    private String prettyType(@Nullable ReferenceNode type) {
        return type != null ? type.value() : "void";
    }
}
