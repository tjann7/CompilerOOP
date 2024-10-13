package ru.team.compiler.tree.node;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;

import java.util.List;

public interface TreeNodeParser<N extends TreeNode> {

    @NotNull
    N parse(@NotNull TokenIterator iterator) throws CompilerException;

    @NotNull
    default N parse(@NotNull List<Token> tokens) throws CompilerException {
        return parse(new TokenIterator(tokens));
    }

    @NotNull
    default N parse(@NotNull Token token) throws CompilerException {
        return parse(List.of(token));
    }

}
