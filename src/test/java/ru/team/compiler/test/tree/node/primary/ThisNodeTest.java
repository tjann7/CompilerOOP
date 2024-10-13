package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.ThisNode;

public class ThisNodeTest {

    @Test
    void parserTest() {
        ThisNode node = ThisNode.PARSER.parse(new Token(TokenType.THIS_KEYWORD, "this"));
        assertEquals(node, new ThisNode());
    }
}
