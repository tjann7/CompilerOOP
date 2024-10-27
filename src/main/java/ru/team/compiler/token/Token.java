package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Token(@NotNull TokenType type, @NotNull String value, int line, int column) {

    public Token(@NotNull TokenType type, @NotNull String value) {
        this(type, value, 0, 0);
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
