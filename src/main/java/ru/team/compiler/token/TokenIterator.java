package ru.team.compiler.token;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;

import java.util.List;

public final class TokenIterator {

    private final List<Token> list;
    private int index = 0;

    public TokenIterator(@NotNull List<Token> list) {
        this.list = list;
    }

    public boolean hasNext() {
        return index < list.size();
    }

    @NotNull
    public Token next() throws CompilerException {
        return next("any");
    }

    @NotNull
    public Token next(@NotNull String expected) throws CompilerException {
        if (!hasNext()) {
            throw new NodeFormatException(expected, NodeFormatException.END_OF_STRING);
        }

        return list.get(index++);
    }

    @NotNull
    public Token next(@NotNull TokenType expected) throws CompilerException {
        return next(expected, expected.name().toLowerCase().replace("_", " "));
    }

    @NotNull
    public Token next(@NotNull TokenType expected, @NotNull String expectedMessage) throws CompilerException {
        if (!hasNext()) {
            throw new NodeFormatException(expectedMessage, NodeFormatException.END_OF_STRING);
        }

        Token token = next(expected.name());
        if (token.type() != expected) {
            throw new NodeFormatException(expectedMessage, token);
        }

        return token;
    }

    @NotNull
    public Token lookup() {
        return lookup("any");
    }

    @NotNull
    public Token lookup(@NotNull String expected) {
        if (!hasNext()) {
            throw new NodeFormatException(expected, NodeFormatException.END_OF_STRING);
        }

        return list.get(index);
    }

    public boolean lookup(@NotNull TokenType expected) {
        return hasNext() && list.get(index).type() == expected;
    }

    public boolean consume(@NotNull TokenType expected) {
        if (lookup(expected)) {
            index++;
            return true;
        }

        return false;
    }

    public void previous() {
        if (index > 0) {
            index--;
        }
    }

    @NotNull
    public TokenIterator copy() {
        TokenIterator iterator = new TokenIterator(list);
        iterator.index = index;
        return iterator;
    }
}
