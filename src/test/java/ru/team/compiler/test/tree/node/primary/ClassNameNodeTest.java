package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.ClassNameNode;

public class ClassNameNodeTest {

    @Test
    void parserTest() {
        String identifier = "abcdef";
        ClassNameNode node = ClassNameNode.PARSER.parse(new Token(TokenType.IDENTIFIER, identifier));
        assertEquals(node, new ClassNameNode(identifier));
    }
}
