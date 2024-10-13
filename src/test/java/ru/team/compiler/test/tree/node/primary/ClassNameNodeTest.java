package ru.team.compiler.test.tree.node.primary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.primary.ClassNameNode;

import java.util.List;

public class ClassNameNodeTest {

    @Test
    void parserTest() {
        String identifier = "abcdef";
        TokenIterator iterator = new TokenIterator(List.of(new Token(TokenType.IDENTIFIER, identifier)));
        ClassNameNode node = ClassNameNode.PARSER.parse(iterator);
        assertEquals(new ClassNameNode(identifier), node);
        assertFalse(iterator.hasNext());
    }
}
