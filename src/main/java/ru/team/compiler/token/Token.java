package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;

public record Token(@NotNull TokenType type, @NotNull String value) {

}
