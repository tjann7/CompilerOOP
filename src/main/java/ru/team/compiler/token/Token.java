package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;

public record Token(@NotNull TokenType type, @NotNull String value, int line, int column) {
    public Token(TokenType type, String value) {
        this(type, value, 0, 0);
    }

    /**
     *
     * @param type
     * @param value
     * @param line increments to avoid 0-line
     * @param column increments to avoid 0-column
     */
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line + 1;
        this.column = column + 1;
    }
    @Override
    public boolean equals(Object other) {
        Token obj = (Token) other;
        return this.type == obj.type && this.value.equals(obj.value);
    }
}
