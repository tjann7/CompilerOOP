package ru.team.compiler.test.tree.node.clas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.SuperNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.ReturnNode;

import java.util.List;

public class ConstructorNodeTest {

    @Test
    void parserTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ConstructorNode node = ConstructorNode.PARSER.parse(iterator);
        assertEquals(new ConstructorNode(
                        false,
                        new ParametersNode(List.of(
                                new ParametersNode.Par(
                                        new IdentifierNode("a"), new ReferenceNode("Integer")))),
                        new BodyNode(List.of(
                                new MethodCallNode(
                                        new ExpressionNode(
                                                new SuperNode(), List.of(
                                                new ExpressionNode.IdArg(
                                                        new IdentifierNode("<init>"),
                                                        new ArgumentsNode(List.of()))))),
                                new ReturnNode(
                                        new ExpressionNode(new ReferenceNode("a"), List.of())))),
                        true),
                node);
        assertFalse(iterator.hasNext());
    }
}
