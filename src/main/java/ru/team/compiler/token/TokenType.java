package ru.team.compiler.token;

public enum TokenType {
    INTEGER_LITERAL,
    REAL_LITERAL,
    BOOLEAN_LITERAL,

    IDENTIFIER,
    UNIDENTIFIED,

    COLON,
    ASSIGNMENT_OPERATOR,
    DOT,
    COMMA,

    SEMICOLON,

    OPENING_PARENTHESIS,
    CLOSING_PARENTHESIS,
    OPENING_BRACKET,
    CLOSING_BRACKET,

    IF_KEYWORD,
    THEN_KEYWORD,
    ELSE_KEYWORD,
    WHILE_KEYWORD,
    LOOP_KEYWORD,
    IS_KEYWORD,
    END_KEYWORD,
    THIS_KEYWORD,
    RETURN_KEYWORD,
    METHOD_KEYWORD,
    CLASS_KEYWORD,
    EXTENDS_KEYWORD,
    VAR_KEYWORD,
    NATIVE_KEYWORD,
    INCLUDE_KEYWORD,
    SUPER_KEYWORD,
    INSTANCEOF_KEYWORD;

    private final boolean literal = name().endsWith("_LITERAL");
    private final boolean keyword = name().endsWith("_KEYWORD");

    public boolean isLiteral() {
        return literal;
    }

    public boolean isKeyword() {
        return keyword;
    }
}
