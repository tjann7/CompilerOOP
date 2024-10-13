package ru.team.compiler.test.tree.node.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.IdentifierNode;

import java.util.List;

public class IdentifierNodeTest {

    @Test
    void parserTest() {
        String identifier = "abcdef";
        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.IDENTIFIER, identifier)));
        IdentifierNode node = IdentifierNode.PARSER.parse(iterator);
        assertEquals(new IdentifierNode(identifier), node);
        assertFalse(iterator.hasNext());
    }
}
