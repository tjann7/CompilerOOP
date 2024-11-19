package ru.team.compiler.test.tree.node.clas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.ReturnNode;

import java.util.List;

public class MethodNodeTest {

    @Test
    void parserVoidTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.METHOD_KEYWORD, "method"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "b"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        MethodNode node = MethodNode.PARSER.parse(iterator);
        assertEquals(new MethodNode(
                        false,
                        false,
                        new IdentifierNode("a"),
                        new ParametersNode(List.of(
                                new ParametersNode.Par(
                                        new IdentifierNode("b"), new ReferenceNode("Integer")))),
                        null,
                        new BodyNode(List.of())),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserWithReturnTypeTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.METHOD_KEYWORD, "method"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "b"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.IDENTIFIER, "b"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        MethodNode node = MethodNode.PARSER.parse(iterator);
        assertEquals(new MethodNode(
                        false,
                        false,
                        new IdentifierNode("a"),
                        new ParametersNode(List.of(
                                new ParametersNode.Par(
                                        new IdentifierNode("b"), new ReferenceNode("Integer")))),
                        new ReferenceNode("Integer"),
                        new BodyNode(List.of(
                                new ReturnNode(
                                        new ExpressionNode(new ReferenceNode("b"), List.of()))))),
                node);
        assertFalse(iterator.hasNext());
    }

}
