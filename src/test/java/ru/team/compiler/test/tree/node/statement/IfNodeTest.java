package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.IfNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.ReturnNode;
import ru.team.compiler.tree.node.statement.StatementNode;
import ru.team.compiler.tree.node.statement.WhileLoopNode;

import java.util.ArrayList;
import java.util.List;

public class IfNodeTest {

    @Test
    void parserEmptyTest() {
        for (int i = 0; i < 2; i++) {
            List<Token> tokens = new ArrayList<>(List.of(
                    new Token(TokenType.IF_KEYWORD, "if"),
                    new Token(TokenType.BOOLEAN_LITERAL, "true"),
                    new Token(TokenType.THEN_KEYWORD, "then"),
                    new Token(TokenType.END_KEYWORD, "end")
            ));

            if (i == 1) {
                tokens.add(3, new Token(TokenType.ELSE_KEYWORD, "else"));
            }

            TokenIterator iterator = new TokenIterator(tokens);
            IfNode node = IfNode.PARSER.parse(iterator);
            assertEquals(new IfNode(
                            new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                            new BodyNode(List.of()),
                            new BodyNode(List.of())),
                    node);
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void parserWithoutElseTest() {
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.IF_KEYWORD, "if"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.THEN_KEYWORD, "then"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.REAL_LITERAL, "1.1"),
                new Token(TokenType.END_KEYWORD, "end")
        ));

        TokenIterator iterator = new TokenIterator(tokens);
        IfNode node = IfNode.PARSER.parse(iterator);
        assertEquals(new IfNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of(
                                new ReturnNode(new ExpressionNode(new RealLiteralNode(1.1), List.of())))),
                        new BodyNode(List.of())),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserWithElseTest() {
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.IF_KEYWORD, "if"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.THEN_KEYWORD, "then"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.REAL_LITERAL, "1.1"),
                new Token(TokenType.ELSE_KEYWORD, "else"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.REAL_LITERAL, "2.2"),
                new Token(TokenType.END_KEYWORD, "end")
        ));

        TokenIterator iterator = new TokenIterator(tokens);
        IfNode node = IfNode.PARSER.parse(iterator);
        assertEquals(new IfNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of(
                                new ReturnNode(new ExpressionNode(new RealLiteralNode(1.1), List.of())))),
                        new BodyNode(List.of(
                                new ReturnNode(new ExpressionNode(new RealLiteralNode(2.2), List.of()))))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void optimizeEmptyBodiesTest() {
        IfNode ifNode = new IfNode(
                new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                new BodyNode(List.of()),
                new BodyNode(List.of())
        );

        List<StatementNode> optimized = ifNode.optimize();

        assertEquals(List.of(), optimized);
    }

    @Test
    void optimizeEmptyBodiesWithMethodConditionTest() {
        ExpressionNode condition = new ExpressionNode(
                new BooleanLiteralNode(true), List.of(
                new ExpressionNode.IdArg(
                        new IdentifierNode("not"), new ArgumentsNode(List.of()))));

        IfNode ifNode = new IfNode(
                condition,
                new BodyNode(List.of()),
                new BodyNode(List.of())
        );

        List<StatementNode> optimized = ifNode.optimize();

        assertEquals(List.of(new MethodCallNode(condition)), optimized);
    }

    @Test
    void optimizeIdenticalBodyTest() {
        ExpressionNode condition = new ExpressionNode(
                new BooleanLiteralNode(true), List.of(
                new ExpressionNode.IdArg(
                        new IdentifierNode("not"), new ArgumentsNode(List.of()))));

        WhileLoopNode body = new WhileLoopNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of()));

        IfNode ifNode = new IfNode(
                condition,
                new BodyNode(List.of(body)),
                new BodyNode(List.of(body))
        );

        List<StatementNode> optimized = ifNode.optimize();

        MethodCallNode methodCall = condition.asMethodCall();
        assertNotNull(methodCall);

        assertEquals(List.of(methodCall, body), optimized);
    }

    @Test
    void optimize() {

    }
}
