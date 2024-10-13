package ru.team.compiler.tree.node.primary;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ThisNode extends PrimaryNode {

    public static final TreeNodeParser<ThisNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ThisNode parse(@NotNull TokenIterator iterator) {
            iterator.next(TokenType.THIS_KEYWORD);
            return new ThisNode();
        }
    };
}
