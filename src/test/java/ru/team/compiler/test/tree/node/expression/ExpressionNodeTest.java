package ru.team.compiler.test.tree.node.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ClassNameNode;
import ru.team.compiler.tree.node.primary.ThisNode;

import java.util.List;

public class ExpressionNodeTest {

    @Test
    void parserSingleTest() {
        String identifier = "abcdef";
        ExpressionNode node = ExpressionNode.PARSER.parse(new Token(TokenType.IDENTIFIER, identifier));
        assertEquals(new ExpressionNode(new ClassNameNode(identifier), List.of()), node);
    }

    @Test
    void parserWithDotTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field1"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field2")
        );

        ExpressionNode node = ExpressionNode.PARSER.parse(tokens);
        List<ExpressionNode.IdArg> list = List.of(
                new ExpressionNode.IdArg(new IdentifierNode("field1"), null),
                new ExpressionNode.IdArg(new IdentifierNode("field2"), null)
        );
        assertEquals(new ExpressionNode(new ThisNode(), list), node);
    }

    @Test
    void parserWithArgumentsTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "method1"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "abc"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "method2"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "cba"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "method3"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        ExpressionNode node = ExpressionNode.PARSER.parse(tokens);
        List<ExpressionNode.IdArg> list = List.of(
                new ExpressionNode.IdArg(new IdentifierNode("method1"), new ArgumentsNode(List.of(
                        new ExpressionNode(
                                new ClassNameNode("abc"),
                                List.of(
                                        new ExpressionNode.IdArg(
                                                new IdentifierNode("method2"),
                                                new ArgumentsNode(List.of(
                                                        new ExpressionNode(
                                                                new ClassNameNode("cba"),
                                                                List.of(
                                                                        new ExpressionNode.IdArg(
                                                                                new IdentifierNode("field"),
                                                                                null)))))))
                        )))),

                new ExpressionNode.IdArg(new IdentifierNode("method3"), new ArgumentsNode(List.of()))
        );
        assertEquals(new ExpressionNode(new ThisNode(), list), node);
    }
}
