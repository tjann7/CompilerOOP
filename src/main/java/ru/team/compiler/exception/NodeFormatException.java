package ru.team.compiler.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NodeFormatException extends RuntimeException {

    public static final Object END_OF_STRING = new Object();

    public NodeFormatException(@Nullable String expected, @Nullable Object actual) {
        super("Expected: %s | Actual: %s".formatted(expected, format(actual)));
    }

    @NotNull
    private static String format(@Nullable Object object) {
        if (object instanceof String string) {
            return '"' + string + '"';
        }

        if (object == END_OF_STRING) {
            return "end of string";
        }

        return Objects.toString(object);
    }
}
