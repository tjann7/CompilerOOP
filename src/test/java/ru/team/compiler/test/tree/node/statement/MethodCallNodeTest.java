package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.StatementNode;

import java.util.List;

public class MethodCallNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.REAL_LITERAL, "1"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new MethodCallNode(
                        new ExpressionNode(
                                new ReferenceNode("a"), List.of(
                                new ExpressionNode.IdArg(
                                        new IdentifierNode("a"),
                                        new ArgumentsNode(List.of(
                                                new ExpressionNode(new RealLiteralNode(1), List.of()))))
                        ))),
                node);
        assertFalse(iterator.hasNext());
    }
}
