package ru.team.compiler.test.tree.node.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.AssignmentNode;
import ru.team.compiler.tree.node.statement.BodyNode;
import ru.team.compiler.tree.node.statement.IfNode;
import ru.team.compiler.tree.node.statement.MethodCallNode;
import ru.team.compiler.tree.node.statement.ReturnNode;
import ru.team.compiler.tree.node.statement.StatementNode;
import ru.team.compiler.tree.node.statement.VariableDeclarationNode;
import ru.team.compiler.tree.node.statement.WhileLoopNode;

import java.util.ArrayList;
import java.util.List;

public class StatementNodeTest {

    @Test
    void parserAssignmentTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.INTEGER_LITERAL, "1")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new AssignmentNode(
                        true,
                        new ReferenceNode("a"),
                        new ExpressionNode(new IntegerLiteralNode(1), List.of())),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserWhileTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.WHILE_KEYWORD, "while"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.LOOP_KEYWORD, "loop"),
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.ASSIGNMENT_OPERATOR, ":="),
                new Token(TokenType.REAL_LITERAL, "5.0"),
                new Token(TokenType.END_KEYWORD, "end")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new WhileLoopNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of(
                                new AssignmentNode(
                                        true,
                                        new ReferenceNode("variable"),
                                        new ExpressionNode(new RealLiteralNode(5), List.of()))))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserIfTest() {
        List<Token> tokens = new ArrayList<>(List.of(
                new Token(TokenType.IF_KEYWORD, "if"),
                new Token(TokenType.BOOLEAN_LITERAL, "true"),
                new Token(TokenType.THEN_KEYWORD, "then"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.REAL_LITERAL, "1.1"),
                new Token(TokenType.ELSE_KEYWORD, "else"),
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.REAL_LITERAL, "2.2"),
                new Token(TokenType.END_KEYWORD, "end")
        ));

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new IfNode(
                        new ExpressionNode(new BooleanLiteralNode(true), List.of()),
                        new BodyNode(List.of(
                                new ReturnNode(new ExpressionNode(new RealLiteralNode(1.1f), List.of())))),
                        new BodyNode(List.of(
                                new ReturnNode(new ExpressionNode(new RealLiteralNode(2.2f), List.of()))))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserReturnTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.RETURN_KEYWORD, "return"),
                new Token(TokenType.IDENTIFIER, "variable"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "field")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new ReturnNode(
                        new ExpressionNode(new ReferenceNode("variable"), List.of(
                                new ExpressionNode.IdArg(new IdentifierNode("field"), null)))),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserVarTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.VAR_KEYWORD, "var"),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.COLON, ":"),
                new Token(TokenType.IDENTIFIER, "Integer")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new VariableDeclarationNode(
                        new IdentifierNode("a"), new ReferenceNode("Integer")),
                node);
        assertFalse(iterator.hasNext());
    }

    @Test
    void parserMethodCallTest() {
        List<Token> tokens = List.of(
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.DOT, "."),
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.OPENING_PARENTHESIS, "("),
                new Token(TokenType.REAL_LITERAL, "1"),
                new Token(TokenType.CLOSING_PARENTHESIS, ")")
        );

        TokenIterator iterator = new TokenIterator(tokens);
        StatementNode node = StatementNode.PARSER.parse(iterator);
        assertEquals(new MethodCallNode(
                        new ExpressionNode(
                                new ReferenceNode("a"), List.of(
                                new ExpressionNode.IdArg(
                                        new IdentifierNode("a"),
                                        new ArgumentsNode(List.of(
                                                new ExpressionNode(new RealLiteralNode(1), List.of()))))
                        ))),
                node);
        assertFalse(iterator.hasNext());
    }
}
