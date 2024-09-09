package ru.team.compiler;

import org.jetbrains.annotations.NotNull;

public record Token(@NotNull TokenType type, @NotNull String value) {

}
