package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Token(@NotNull TokenType type, @NotNull String value, int line, int column) {

    public Token(TokenType type, String value) {
        this(type, value, -1, -1);
    }

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line + 1;
        this.column = column + 1;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Token token = (Token) object;
        return Objects.equals(value, token.value) && type == token.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
