package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
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
            PrimaryNode node = PrimaryNode.PARSER.parse(new Token(TokenType.INTEGER_LITERAL, value));
            assertEquals(new IntegerLiteralNode(Integer.parseInt(value)), node);
        }
    }

    @Test
    void parserRealTest() {
        for (String value : List.of("123.0", "456.0", "789123.0")) {
            PrimaryNode node = PrimaryNode.PARSER.parse(new Token(TokenType.REAL_LITERAL, value));
            assertEquals(new RealLiteralNode(Double.parseDouble(value)), node);
        }
    }

    @Test
    void parserBooleanTest() {
        for (String value : List.of("true", "false")) {
            PrimaryNode node = PrimaryNode.PARSER.parse(new Token(TokenType.BOOLEAN_LITERAL, value));
            assertEquals(new BooleanLiteralNode(Boolean.parseBoolean(value)), node);
        }
    }

    @Test
    void parserThisTest() {
        PrimaryNode node = PrimaryNode.PARSER.parse(new Token(TokenType.THIS_KEYWORD, "this"));
        assertEquals(new ThisNode(), node);
    }

    @Test
    void parserClassTest() {
        for (String value : List.of("a", "b", "c")) {
            PrimaryNode node = PrimaryNode.PARSER.parse(new Token(TokenType.IDENTIFIER, value));
            assertEquals(new ClassNameNode(value), node);
        }
    }
}
