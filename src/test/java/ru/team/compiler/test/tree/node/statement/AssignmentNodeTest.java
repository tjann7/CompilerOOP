package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.statement.AssignmentNode;

import java.util.List;

public class AssignmentNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.REAL_LITERAL, "5.0")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        AssignmentNode node = AssignmentNode.PARSER.parse(iterator);
        assertEquals(new AssignmentNode(
                        new IdentifierNode("variable"),
                        new ExpressionNode(new RealLiteralNode(5), List.of())),
                node);
    }
}
