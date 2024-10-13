package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.ThisNode;

import java.util.List;

public class ThisNodeTest {

    @Test
    void parserTest() {
        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.THIS_KEYWORD, "this")));
        ThisNode node = ThisNode.PARSER.parse(iterator);
        assertEquals(node, new ThisNode());
        assertFalse(iterator.hasNext());
    }
}
