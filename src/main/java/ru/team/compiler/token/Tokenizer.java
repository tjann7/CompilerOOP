package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Tokenizer {

    private final Pattern identifierPattern = Pattern.compile("[a-zA-Z0-9]+");
    private final Map<Character, TokenType> singleTokens = Map.of(
            '.', TokenType.DOT,
            ',', TokenType.COMMA,
            ':', TokenType.COLON,
            '(', TokenType.OPENING_PARENTHESIS,
            ')', TokenType.CLOSING_PARENTHESIS,
            '[', TokenType.OPENING_BRACKET,
            ']', TokenType.CLOSING_BRACKET,
            ';', TokenType.SEMICOLON
    );
    private final Map<String, TokenType> keywords = Arrays.stream(TokenType.values())
        .filter(tokenType -> tokenType.name().endsWith("_KEYWORD"))
        .map(tokenType -> {
            String name = tokenType.name();
            return Pair.of(name.substring(0, name.length() - "_KEYWORD".length()).toLowerCase(), tokenType);
        })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    private final Set<String> booleans = Set.of(
            "true", "false"
    );

    private final String string;
    private int line;
    private int pos;

    public Tokenizer(String string, int line) {
        this.string = string;
        this.line = line;
    }

    public Tokenizer(String string) {
        this.string = string;
        this.line = 0;
    }

    public boolean hasNext() {
        skipWhitespaces(false);

        return !end();
    }

    @NotNull
    public Token next() {
        String token = token();

        if (token.length() == 1) {
            TokenType tokenType = singleTokens.get(token.charAt(0));
            if (tokenType != null) {
                return new Token(tokenType, token, line, pos);
            }
        }

        if (token.equals(":=")) {
            return new Token(TokenType.ASSIGNMENT_OPERATOR, token, line, pos);
        }

        TokenType tokenType = keywords.get(token);
        if (tokenType != null) {
            return new Token(tokenType, token, line, pos);
        }

        if (booleans.contains(token)) {
            return new Token(TokenType.BOOLEAN_LITERAL, token, line, pos);
        }

        char startChar = token.charAt(0);
        if (Character.isDigit(startChar)) {
            boolean dot = false;

            for (char chr : token.toCharArray()) {
                if (chr == '.') {
                    if (dot) {
                        return new Token(TokenType.UNIDENTIFIED, token, line, pos);
                    }

                    dot = true;
                    continue;
                }

                if (!Character.isDigit(chr)) {
                    return new Token(TokenType.UNIDENTIFIED, token, line, pos);
                }
            }

            return new Token(dot ? TokenType.REAL_LITERAL : TokenType.INTEGER_LITERAL, token, line, pos);
        }

        if (identifierPattern.matcher(token).matches()) {
            return new Token(TokenType.IDENTIFIER, token, line, pos);
        }

        return new Token(TokenType.UNIDENTIFIED, token, line, pos);
    }

    // ===

    private void skipWhitespaces() {
        skipWhitespaces(true);
    }

    private void skipWhitespaces(boolean checkEof) {
        while (pos < string.length() && Character.isWhitespace(string.charAt(pos))) {
            pos++;
        }

        if (checkEof) {
            checkEof();
        }
    }

    private char chr() {
        checkEof();

        pos++;
        return string.charAt(pos - 1);
    }

    @NotNull
    private String token() {
        skipWhitespaces();

        StringBuilder stringBuilder = new StringBuilder();

        char c = chr();
        TokenType tokenType = singleTokens.get(c);
        if (tokenType != null) {
            if (tokenType == TokenType.COLON) {
                if (!end()) {
                    char c1 = chr();
                    if (c1 == '=') {
                        return ":=";
                    } else {
                        pos--;
                    }
                }
            }

            return Character.toString(c);
        }

        boolean ignoreDot = Character.isDigit(c);

        while (!Character.isWhitespace(c)) {
            stringBuilder.append(c);
            if (end()) {
                break;
            }

            c = chr();

            if (singleTokens.containsKey(c)) {
                if (ignoreDot && c == '.') {
                    continue;
                }

                pos--;
                return stringBuilder.toString();
            }
        }

        return stringBuilder.toString();
    }

    private boolean end() {
        return pos >= string.length();
    }

    private void checkEof() {
        if (end()) {
            throw expected("string", "end of string");
        }
    }

    @NotNull
    private IllegalArgumentException expected(Object expected, Object actual) {
        return new IllegalArgumentException("Expected: " + expected + " | Actual: " + actual);
    }
}
