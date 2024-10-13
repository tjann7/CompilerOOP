package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.RealLiteralNode;

import java.util.List;

public class RealLiteralNodeTest {

    @Test
    void parserTest() {
        for (String value : List.of("1.5", "123.2", "5892.9")) {
            RealLiteralNode node = RealLiteralNode.PARSER.parse(new Token(TokenType.REAL_LITERAL, value));
            assertEquals(node, new RealLiteralNode(Double.parseDouble(value)));
        }
    }
}
