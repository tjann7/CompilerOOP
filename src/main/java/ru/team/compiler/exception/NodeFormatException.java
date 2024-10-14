package ru.team.compiler.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.token.Token;

import java.util.Objects;

public class NodeFormatException extends RuntimeException {

    public static final Object END_OF_STRING = new Object();

    public NodeFormatException(@Nullable String expected, @Nullable Token actual) {
        this(expected, actual, actual);
    }

    public NodeFormatException(@Nullable String expected, @Nullable Object actual, @Nullable Token token) {
        this(expected, actual, token != null ? token.line() : -1, token != null ? token.column() : -1);
    }

    public NodeFormatException(@Nullable String expected, @Nullable Object actual, int line, int column) {
        super("(at line: %d, column: %d) Expected: %s | Actual: %s".formatted(line, column, expected, format(actual)));
    }

    @NotNull
    private static String format(@Nullable Object object) {
        if (object instanceof String string) {
            return '"' + string + '"';
        }

        if (object instanceof Token token) {
            return token.type().name().toLowerCase();
        }

        if (object == END_OF_STRING) {
            return "end of string";
        }

        return Objects.toString(object);
    }
}
