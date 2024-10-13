package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;

import java.util.List;

public class BooleanLiteralNodeTest {

    @Test
    void parserTest() {
        for (String value : List.of("true", "false")) {
            BooleanLiteralNode node = BooleanLiteralNode.PARSER.parse(new Token(TokenType.BOOLEAN_LITERAL, value));
            assertEquals(node, new BooleanLiteralNode(Boolean.parseBoolean(value)));
        }
    }
}
