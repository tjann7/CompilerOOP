package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ClassNameNode;
import ru.team.compiler.tree.node.statement.ReturnNode;

import java.util.List;

public class ReturnNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ReturnNode node = ReturnNode.PARSER.parse(iterator);
        assertEquals(new ReturnNode(
                new ExpressionNode(new ClassNameNode("variable"), List.of(
                        new ExpressionNode.IdArg(new IdentifierNode("field"), null)))),
                node);
        assertFalse(iterator.hasNext());
    }
}
