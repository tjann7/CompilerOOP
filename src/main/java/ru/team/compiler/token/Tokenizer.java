package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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
    private int line = 1;
    private int column;
    private int columnShift;
    private int pos;

    public Tokenizer(String string) {
        this.string = string;
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
                return new Token(tokenType, token, line, column);
            }
        }

        if (token.equals(":=")) {
            return new Token(TokenType.ASSIGNMENT_OPERATOR, token, line, column);
        }

        TokenType tokenType = keywords.get(token);
        if (tokenType != null) {
            return new Token(tokenType, token, line, column);
        }

        if (booleans.contains(token)) {
            return new Token(TokenType.BOOLEAN_LITERAL, token, line, column);
        }

        char startChar = token.charAt(0);
        if (Character.isDigit(startChar)) {
            boolean dot = false;

            for (char chr : token.toCharArray()) {
                if (chr == '.') {
                    if (dot) {
                        return new Token(TokenType.UNIDENTIFIED, token, line, column);
                    }

                    dot = true;
                    continue;
                }

                if (!Character.isDigit(chr)) {
                    return new Token(TokenType.UNIDENTIFIED, token, line, column);
                }
            }

            return new Token(dot ? TokenType.REAL_LITERAL : TokenType.INTEGER_LITERAL, token, line, column);
        }

        if (identifierPattern.matcher(token).matches()) {
            return new Token(TokenType.IDENTIFIER, token, line, column);
        }

        return new Token(TokenType.UNIDENTIFIED, token, line, column);
    }

    // ===

    private void skipWhitespaces() {
        skipWhitespaces(true);
    }

    private void skipWhitespaces(boolean checkEof) {
        while (pos < string.length()) {
            char chr = string.charAt(pos);
            if (Character.isWhitespace(chr)) {
                onWhitespace(chr);
                pos++;
            } else {
                break;
            }
        }

        if (checkEof) {
            checkEof();
        }
    }

    private void onWhitespace(char chr) {
        if (chr == '\n') {
            line++;
            column = 0;
            columnShift = 0;
        } else if (chr == '\t') {
            column += 4;
        } else if (Character.isWhitespace(chr)) {
            column++;
        }
    }

    private char chr() {
        checkEof();

        columnShift++;
        pos++;
        return string.charAt(pos - 1);
    }

    @NotNull
    private String token() {
        skipWhitespaces();

        StringBuilder stringBuilder = new StringBuilder();

        column += columnShift;
        columnShift = 0;

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
                columnShift--;
                pos--;
                return stringBuilder.toString();
            }
        }

        if (!end()) {
            pos--;
            columnShift--;
        }

        return stringBuilder.toString();
    }

    private boolean end() {
        return pos >= string.length();
    }

    private void checkEof() {
        if (end()) {
            throw expected("string", NodeFormatException.END_OF_STRING);
        }
    }

    @NotNull
    private NodeFormatException expected(@Nullable Object expected, @Nullable Object actual) {
        return new NodeFormatException(Objects.toString(expected), actual, line, column);
    }
}
