package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.List;

public class ReferenceNodeTest {

    @Test
    void parserTest() {
        String identifier = "abcdef";
        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.IDENTIFIER, identifier)));
        ReferenceNode node = ReferenceNode.PARSER.parse(iterator);
        assertEquals(new ReferenceNode(identifier), node);
        assertFalse(iterator.hasNext());
    }
}
