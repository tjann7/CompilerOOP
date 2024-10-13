package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;

public record Token(@NotNull TokenType type, @NotNull String value, int line, int column) {
    public Token(TokenType type, String value) {
        this(type, value, 0, 0);
    }
    @Override
    public boolean equals(Object other) {
        Token obj = (Token) other;
        return this.type == obj.type && this.value.equals(obj.value);
    }
}
