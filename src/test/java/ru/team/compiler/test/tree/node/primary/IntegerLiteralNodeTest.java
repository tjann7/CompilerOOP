package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;

import java.util.List;

public class IntegerLiteralNodeTest {

    @Test
    void parserTest() {
        for (String value : List.of("1", "123", "5892")) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.INTEGER_LITERAL, value)));
            IntegerLiteralNode node = IntegerLiteralNode.PARSER.parse(iterator);
            assertEquals(node, new IntegerLiteralNode(Integer.parseInt(value)));
            assertFalse(iterator.hasNext());
        }
    }
}
