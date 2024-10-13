package ru.team.compiler.test.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.test.util.RandomChar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TokenizerTest {

    @Test
    void identifier() {
        String identifierToken = "TestIdentifier";
        test(new Tokenizer(identifierToken), new Token(TokenType.IDENTIFIER, identifierToken));
    }

    @Test
    void brackets() {
        String openingParenthesisLiteral = "(";
        test(new Tokenizer(openingParenthesisLiteral), new Token(TokenType.OPENING_PARENTHESIS, openingParenthesisLiteral));

        String closingParenthesisLiteral = ")";
        test(new Tokenizer(closingParenthesisLiteral), new Token(TokenType.CLOSING_PARENTHESIS, closingParenthesisLiteral));

        String openingBracketLiteral = "[";
        test(new Tokenizer(openingBracketLiteral), new Token(TokenType.OPENING_BRACKET, openingBracketLiteral));

        String closingBracketLiteral = "]";
        test(new Tokenizer(closingBracketLiteral), new Token(TokenType.CLOSING_BRACKET, closingBracketLiteral));
    }

    @Test
    void keywords() {
        for (TokenType tokenType : TokenType.values()) {
            String name = tokenType.name();
            if (!name.endsWith("_KEYWORD")) {
                continue;
            }

            String literal = name.substring(0, name.length() - "_KEYWORD".length()).toLowerCase();

            Tokenizer tokenizer = new Tokenizer(literal);
            test(tokenizer, new Token(tokenType, literal));
        }
    }

    @Test
    void literals() {
        String integerLiteral = "1234";
        test(new Tokenizer(integerLiteral), new Token(TokenType.INTEGER_LITERAL, integerLiteral));

        String realLiteral = "1234.56";
        test(new Tokenizer(realLiteral), new Token(TokenType.REAL_LITERAL, realLiteral));

        String falseLiteral = "false";
        test(new Tokenizer(falseLiteral), new Token(TokenType.BOOLEAN_LITERAL, falseLiteral));

        String trueLiteral = "true";
        test(new Tokenizer(trueLiteral), new Token(TokenType.BOOLEAN_LITERAL, trueLiteral));
    }

    @Test
    void unidentified() {
        String unidentifiedToken = "1L";
        test(new Tokenizer(unidentifiedToken), new Token(TokenType.UNIDENTIFIED, unidentifiedToken));

        unidentifiedToken = "abc=";
        test(new Tokenizer(unidentifiedToken), new Token(TokenType.UNIDENTIFIED, unidentifiedToken));
    }

    @Test
    void singleTokens() {
        String colonToken = ":";
        test(new Tokenizer(colonToken), new Token(TokenType.COLON, colonToken));

        String assignmentOperatorToken = ":=";
        test(new Tokenizer(assignmentOperatorToken), new Token(TokenType.ASSIGNMENT_OPERATOR, assignmentOperatorToken));

        String dotToken = ".";
        test(new Tokenizer(dotToken), new Token(TokenType.DOT, dotToken));

        String commaToken = ",";
        test(new Tokenizer(commaToken), new Token(TokenType.COMMA, commaToken));
    }

    @Test
    void semicolonTokens() {
        String semicolonTokens = "a ;  b ;\t\nc\n";
        test(new Tokenizer(semicolonTokens), List.of(
                new Token(TokenType.IDENTIFIER, "a"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IDENTIFIER, "b"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.IDENTIFIER, "c")
        ));
    }

    @Test
    void complexTokens() {
        List<Token> availableTokens = new ArrayList<>();

        Random random = new Random(12345);
        RandomChar randomChar = new RandomChar(random);

        // Prepare identifiers
        for (int i = 0; i < 10; i++) {
            StringBuilder identifier = new StringBuilder();

            identifier.append(randomChar.nextUpperCaseLetter());
            for (int j = 0; j < random.nextInt(10, 20); j++) {
                identifier.append(randomChar.next());
            }

            availableTokens.add(new Token(TokenType.IDENTIFIER, identifier.toString()));
        }

        // Prepare integer literals
        for (int i = 0; i < 5; i++) {
            StringBuilder integerLiteral = new StringBuilder();

            for (int j = 0; j < random.nextInt(3, 8); j++) {
                integerLiteral.append(randomChar.nextDigit());
            }

            availableTokens.add(new Token(TokenType.INTEGER_LITERAL, integerLiteral.toString()));
        }

        // Prepare real literals
        for (int i = 0; i < 5; i++) {
            StringBuilder realLiteral = new StringBuilder();

            for (int j = 0; j < random.nextInt(3, 8); j++) {
                realLiteral.append(randomChar.nextDigit());
            }

            realLiteral.append(".");

            for (int j = 0; j < random.nextInt(1, 4); j++) {
                realLiteral.append(randomChar.nextDigit());
            }

            availableTokens.add(new Token(TokenType.REAL_LITERAL, realLiteral.toString()));
        }

        // Prepare boolean literals
        availableTokens.add(new Token(TokenType.BOOLEAN_LITERAL, "false"));
        availableTokens.add(new Token(TokenType.BOOLEAN_LITERAL, "true"));

        // Prepare brackets
        availableTokens.add(new Token(TokenType.OPENING_PARENTHESIS, "("));
        availableTokens.add(new Token(TokenType.CLOSING_PARENTHESIS, ")"));
        availableTokens.add(new Token(TokenType.OPENING_BRACKET, "["));
        availableTokens.add(new Token(TokenType.CLOSING_BRACKET, "]"));

        // Prepare single
        availableTokens.add(new Token(TokenType.COLON, ":"));
        availableTokens.add(new Token(TokenType.ASSIGNMENT_OPERATOR, ":="));
        availableTokens.add(new Token(TokenType.DOT, "."));
        availableTokens.add(new Token(TokenType.COMMA, ","));

        // Prepare keywords
        for (TokenType tokenType : TokenType.values()) {
            String name = tokenType.name();
            if (!name.endsWith("_KEYWORD")) {
                continue;
            }

            String literal = name.substring(0, name.length() - "_KEYWORD".length()).toLowerCase();
            availableTokens.add(new Token(tokenType, literal));
        }

        // Generating random code
        List<String> delimiters = List.of(
                " ", "\t", "\n", ";"
        );

        for (int i = 0; i < 1000; i++) {
            List<Token> tokens = new ArrayList<>();

            StringBuilder codeBuilder = new StringBuilder();

            for (int j = 0; j < 10; j++) {
                Token token = availableTokens.get(random.nextInt(availableTokens.size()));
                tokens.add(token);

                codeBuilder.append(token.value());

                TokenType tokenType = token.type();
                boolean mustUseSpace = tokenType.isKeyword() || tokenType.isLiteral() || tokenType == TokenType.IDENTIFIER;

                if (mustUseSpace || random.nextBoolean()) {
                    String delimiter = delimiters.get(random.nextInt(delimiters.size()));
                    codeBuilder.append(delimiter);

                    if (delimiter.equals(";")) {
                        tokens.add(new Token(TokenType.SEMICOLON, ";"));
                    }
                }
            }

            Tokenizer tokenizer = new Tokenizer(codeBuilder.toString());

            int fi = i;

            for (Token token : tokens) {
                assertTrue(tokenizer.hasNext(), () -> "Test #" + (fi + 1) + " failed: " + codeBuilder);
                assertEquals(token, tokenizer.next(), () -> "Test #" + (fi + 1) + " failed: " + codeBuilder);
            }

            assertFalse(tokenizer.hasNext(), () -> "Test #" + (fi + 1) + " failed: " + codeBuilder);
        }
    }

    private void test(@NotNull Tokenizer tokenizer, @NotNull Token @NotNull ... expectedTokens) {
        test(tokenizer, List.of(expectedTokens));
    }

    private void test(@NotNull Tokenizer tokenizer, @NotNull Iterable<Token> expectedTokens) {
        for (Token token : expectedTokens) {
            assertTrue(tokenizer.hasNext(), token::toString);
            assertEquals(token, tokenizer.next());
        }

        assertFalse(tokenizer.hasNext());
    }
}
