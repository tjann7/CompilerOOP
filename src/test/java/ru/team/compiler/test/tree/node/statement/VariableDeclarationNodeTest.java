package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.VariableDeclarationNode;

import java.util.List;

public class VariableDeclarationNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.VAR_KEYWORD, "var"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        VariableDeclarationNode node = VariableDeclarationNode.PARSER.parse(iterator);
        assertEquals(new VariableDeclarationNode(
                        new IdentifierNode("a"), new ReferenceNode("Integer")),
                node);
        assertFalse(iterator.hasNext());
    }
}
