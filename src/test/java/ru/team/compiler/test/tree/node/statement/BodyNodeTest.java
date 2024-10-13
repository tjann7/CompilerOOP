package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.statement.AssignmentNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.WhileLoopNode;

import java.util.ArrayList;
import java.util.List;

public class BodyNodeTest {

    @Test
    void parserEmptyTest() {
        TreeNodeParser<BodyNode> parser = BodyNode.parser(TokenType.END_KEYWORD);

        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.END_KEYWORD, "end")));
        BodyNode node = parser.parse(iterator);
        assertEquals(new BodyNode(List.of()), node);
        assertTrue(iterator.hasNext());
        assertEquals(TokenType.END_KEYWORD, iterator.next().type());
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserMultipleEndTest() {
        TreeNodeParser<BodyNode> parser = BodyNode.parser(TokenType.ELSE_KEYWORD, TokenType.END_KEYWORD);

        for (TokenType tokenType : List.of(TokenType.ELSE_KEYWORD, TokenType.END_KEYWORD)) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(tokenType, "else")));
            BodyNode node = parser.parse(iterator);
            assertEquals(new BodyNode(List.of()), node);
            assertTrue(iterator.hasNext());
            assertEquals(tokenType, iterator.next().type());
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void parserSingleTest() {
        TreeNodeParser<BodyNode> parser = BodyNode.parser(TokenType.END_KEYWORD);

        for (int i = 0; i < 2; i++) {
            List<Token> tokens = new ArrayList<>(List.of(
                    new Token(TokenType.IDENTIFIER, "variable"),
                    new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                    new Token(TokenType.REAL_LITERAL, "5.0"),
                    new Token(TokenType.END_KEYWORD, "end")
            ));

            if (i == 1) {
                tokens.add(3, new Token(TokenType.SEMICOLON, ";"));
            }

            TokenIterator iterator = new TokenIterator(tokens);
            BodyNode node = parser.parse(iterator);
            assertEquals(new BodyNode(List.of(
                            new AssignmentNode(
                                    new ExpressionNode(new ReferenceNode("variable"), List.of()),
                                    new ExpressionNode(new RealLiteralNode(5), List.of())))),
                    node, "Test #" + (i + 1));
            assertTrue(iterator.hasNext(), "Test #" + (i + 1));
            assertEquals(TokenType.END_KEYWORD, iterator.next().type(), "Test #" + (i + 1));
            assertFalse(iterator.hasNext(), "Test #" + (i + 1));
        }
    }

    @Test
    void parserMultipleTest() {
        TreeNodeParser<BodyNode> parser = BodyNode.parser(TokenType.END_KEYWORD);

        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "variable1"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.IDENTIFIER, "id1"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field1"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IDENTIFIER, "variable2"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.BOOLEAN_LITERAL, "false"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        BodyNode node = parser.parse(iterator);
        assertEquals(new BodyNode(List.of(
                        new AssignmentNode(
                                new ExpressionNode(new ReferenceNode("variable1"), List.of()),
                                new ExpressionNode(new ReferenceNode("id1"), List.of(
                                        new ExpressionNode.IdArg(new IdentifierNode("field1"), null)
                                ))),
                        new AssignmentNode(
                                new ExpressionNode(new ReferenceNode("variable2"), List.of()),
                                new ExpressionNode(new BooleanLiteralNode(false), List.of())
                        ))),
                node);
        assertTrue(iterator.hasNext());
        assertEquals(TokenType.END_KEYWORD, iterator.next().type());
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserRecursiveTest() {
        TreeNodeParser<BodyNode> parser = BodyNode.parser(TokenType.END_KEYWORD);

        List<Token> tokens = List.of(
                new Token(TokenType.WHILE_KEYWORD, "while"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.LOOP_KEYWORD, "loop"),
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.REAL_LITERAL, "5.0"),
                new Token(TokenType.END_KEYWORD, "end"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        BodyNode node = parser.parse(iterator);
        assertEquals(new BodyNode(List.of(
                        new WhileLoopNode(
                                new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                                new BodyNode(List.of(
                                        new AssignmentNode(
                                                new ExpressionNode(new ReferenceNode("variable"), List.of()),
                                                new ExpressionNode(new RealLiteralNode(5), List.of()))))))),
                node);
        assertTrue(iterator.hasNext());
        assertEquals(TokenType.END_KEYWORD, iterator.next().type());
        assertFalse(iterator.hasNext());
    }
}
