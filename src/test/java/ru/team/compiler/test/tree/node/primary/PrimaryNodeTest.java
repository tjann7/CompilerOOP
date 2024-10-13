package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ClassNameNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ThisNode;

import java.util.List;

public class PrimaryNodeTest {

    @Test
    void parserIntegerTest() {
        for (String value : List.of("123", "456", "789123")) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.INTEGER_LITERAL, value)));
            PrimaryNode node = PrimaryNode.PARSER.parse(iterator);
            assertEquals(new IntegerLiteralNode(Integer.parseInt(value)), node);
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void parserRealTest() {
        for (String value : List.of("123.0", "456.0", "789123.0")) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.REAL_LITERAL, value)));
            PrimaryNode node = PrimaryNode.PARSER.parse(iterator);
            assertFalse(iterator.hasNext());
            assertEquals(new RealLiteralNode(Double.parseDouble(value)), node);
        }
    }

    @Test
    void parserBooleanTest() {
        for (String value : List.of("true", "false")) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.BOOLEAN_LITERAL, value)));
            PrimaryNode node = PrimaryNode.PARSER.parse(iterator);
            assertEquals(new BooleanLiteralNode(Boolean.parseBoolean(value)), node);
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void parserThisTest() {
        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.THIS_KEYWORD, "this")));
        PrimaryNode node = PrimaryNode.PARSER.parse(iterator);
        assertEquals(new ThisNode(), node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserClassTest() {
        for (String value : List.of("a", "b", "c")) {
            TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.IDENTIFIER, value)));
            PrimaryNode node = PrimaryNode.PARSER.parse(iterator);
            assertEquals(new ClassNameNode(value), node);
            assertFalse(iterator.hasNext());
        }
    }
}
