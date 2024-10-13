package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.statement.AssignmentNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.ReturnNode;
import ru.team.compiler.tree.node.statement.WhileLoopNode;

import java.util.List;

public class WhileLoopTest {

    @Test
    void parserEmptyTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.WHILE_KEYWORD, "while"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.LOOP_KEYWORD, "loop"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        WhileLoopNode node = WhileLoopNode.PARSER.parse(iterator);
        assertEquals(new WhileLoopNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of())),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserWithBodyTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.WHILE_KEYWORD, "while"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.LOOP_KEYWORD, "loop"),
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.REAL_LITERAL, "5.0"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.INTEGER_LITERAL, "1"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        WhileLoopNode node = WhileLoopNode.PARSER.parse(iterator);
        assertEquals(new WhileLoopNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of(
                                new AssignmentNode(
                                        new IdentifierNode("variable"),
                                        new ExpressionNode(new RealLiteralNode(5), List.of())),
                                new ReturnNode(
                                        new ExpressionNode(new IntegerLiteralNode(1), List.of()))))),
                node);
        assertFalse(iterator.hasNext());
    }
}
