package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class AssignmentNode extends StatementNode {

    public static final TreeNodeParser<AssignmentNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public AssignmentNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);
            iterator.next(TokenType.ASSIGNMENT_OPERATOR);
            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);
            return new AssignmentNode(identifierNode, expressionNode);
        }
    };

    private final IdentifierNode identifierNode;
    private final ExpressionNode expressionNode;

    public AssignmentNode(@NotNull IdentifierNode identifierNode, @NotNull ExpressionNode expressionNode) {
        this.identifierNode = identifierNode;
        this.expressionNode = expressionNode;
    }

    @NotNull
    public IdentifierNode identifierNode() {
        return identifierNode;
    }

    @NotNull
    public ExpressionNode expressionNode() {
        return expressionNode;
    }
}
