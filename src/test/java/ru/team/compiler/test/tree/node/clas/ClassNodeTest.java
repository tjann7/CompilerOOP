package ru.team.compiler.test.tree.node.clas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.FieldNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.AssignmentNode;
import ru.team.compiler.tree.node.statement.BodyNode;

import java.util.List;

public class ClassNodeTest {

    @Test
    void parserEmptyTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.CLASS_KEYWORD, "class"),
                new Token(TokenType.IDENTIFIER, "A"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ClassNode node = ClassNode.PARSER.parse(iterator);
        assertEquals(new ClassNode(
                        new IdentifierNode("A"),
                        null,
                        List.of()),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserEmptyExtendsTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.CLASS_KEYWORD, "class"),
                new Token(TokenType.IDENTIFIER, "A"),
                new Token(TokenType.EXTENDS_KEYWORD, "extends"),
                new Token(TokenType.IDENTIFIER, "B"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ClassNode node = ClassNode.PARSER.parse(iterator);
        assertEquals(new ClassNode(
                        new IdentifierNode("A"),
                        new ReferenceNode("B"),
                        List.of()),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserSingleTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.CLASS_KEYWORD, "class"),
                new Token(TokenType.IDENTIFIER, "A"),
                new Token(TokenType.EXTENDS_KEYWORD, "extends"),
                new Token(TokenType.IDENTIFIER, "B"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.VAR_KEYWORD, "var"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ClassNode node = ClassNode.PARSER.parse(iterator);
        assertEquals(new ClassNode(
                        new IdentifierNode("A"),
                        new ReferenceNode("B"),
                        List.of(
                                new FieldNode(new IdentifierNode("a"), new ReferenceNode("Integer")))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserMultipleTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.CLASS_KEYWORD, "class"),
                new Token(TokenType.IDENTIFIER, "A"),
                new Token(TokenType.EXTENDS_KEYWORD, "extends"),
                new Token(TokenType.IDENTIFIER, "B"),
                new Token(TokenType.IS_KEYWORD, "is"),

                new Token(TokenType.VAR_KEYWORD, "var"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),

                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")"),
                new Token(TokenType.IS_KEYWORD, "is"),
                new Token(TokenType.THIS_KEYWORD, "this"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.END_KEYWORD, "end"),

                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        ClassNode node = ClassNode.PARSER.parse(iterator);
        assertEquals(new ClassNode(
                        new IdentifierNode("A"),
                        new ReferenceNode("B"),
                        List.of(
                                new FieldNode(new IdentifierNode("a"), new ReferenceNode("Integer")),
                                new ConstructorNode(
                                        false,
                                        new ParametersNode(List.of(
                                                new ParametersNode.Par(new IdentifierNode("a"),
                                                        new ReferenceNode("Integer")))),
                                        new BodyNode(List.of(
                                                new AssignmentNode(
                                                        false,
                                                        new ReferenceNode("a"),
                                                        new ExpressionNode(new ReferenceNode("a"), List.of()))
                                        ))))),
                node);
        assertFalse(iterator.hasNext());
    }
}
