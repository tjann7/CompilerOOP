package ru.team.compiler.test.tree.node.clas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;

public class ParametersNodeTest {

    @Test
    void parserEmptyTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ParametersNode node = ParametersNode.PARSER.parse(iterator);
        assertEquals(new ParametersNode(List.of()),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserSingleTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ParametersNode node = ParametersNode.PARSER.parse(iterator);
        assertEquals(new ParametersNode(List.of(
                        new ParametersNode.Par(new IdentifierNode("a"), new ReferenceNode("Integer")))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserMultipleTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENTIFIER, "b"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Real"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ParametersNode node = ParametersNode.PARSER.parse(iterator);
        assertEquals(new ParametersNode(List.of(
                        new ParametersNode.Par(new IdentifierNode("a"), new ReferenceNode("Integer")),
                        new ParametersNode.Par(new IdentifierNode("b"), new ReferenceNode("Real")))),
                node);
        assertFalse(iterator.hasNext());
    }
}
