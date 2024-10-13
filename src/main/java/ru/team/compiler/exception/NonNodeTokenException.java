package ru.team.compiler.exception;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.token.Token;

public class NonNodeTokenException extends RuntimeException {

    public NonNodeTokenException(@NotNull Token token) {
        super("Token %s does not represent a node".formatted(token));
    }
}
