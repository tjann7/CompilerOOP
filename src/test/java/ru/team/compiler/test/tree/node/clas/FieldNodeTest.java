package ru.team.compiler.test.tree.node.clas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;

public class FieldNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.VAR_KEYWORD, "var"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.SEMICOLON, ";")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        FieldNode node = FieldNode.PARSER.parse(iterator);
        assertEquals(new FieldNode(
                        new IdentifierNode("a"), new ReferenceNode("Integer")),
                node);
        assertFalse(iterator.hasNext());
    }

}
