package ru.team.compiler.test.tree.node.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.IdentifierNode;

public class IdentifierNodeTest {

    @Test
    void parserTest() {
        String identifier = "abcdef";
        IdentifierNode node = IdentifierNode.PARSER.parse(new Token(TokenType.IDENTIFIER, identifier));
        assertEquals(new IdentifierNode(identifier), node);
    }
}
