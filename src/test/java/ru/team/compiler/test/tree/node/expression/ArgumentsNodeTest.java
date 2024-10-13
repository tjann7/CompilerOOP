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

public class ArgumentsNodeTest {

    @Test
    void parserEmptyTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        ArgumentsNode node = ArgumentsNode.PARSER.parse(tokens);
        assertEquals(new ArgumentsNode(List.of()), node);
    }

    @Test
    void parserSingleTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        ArgumentsNode node = ArgumentsNode.PARSER.parse(tokens);
        assertEquals(new ArgumentsNode(List.of(new ExpressionNode(new ThisNode(), List.of()))), node);
    }

    @Test
    void parserMoreTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENTIFIER, "abc"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        ArgumentsNode node = ArgumentsNode.PARSER.parse(tokens);
        assertEquals(new ArgumentsNode(List.of(
                new ExpressionNode(new ThisNode(), List.of()),

                new ExpressionNode(new ClassNameNode("abc"), List.of(
                        new ExpressionNode.IdArg(new IdentifierNode("field"), null)))
        )), node);
    }

    @Test
    void parserRecursiveTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "abc"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "method"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        ArgumentsNode node = ArgumentsNode.PARSER.parse(tokens);
        assertEquals(new ArgumentsNode(List.of(
                new ExpressionNode(new ClassNameNode("abc"), List.of(
                        new ExpressionNode.IdArg(
                                new IdentifierNode("method"),
                                new ArgumentsNode(List.of(
                                        new ExpressionNode(new ThisNode(), List.of())
                                )))))
        )), node);
    }
}
