package ru.team.compiler.tree.node.primary;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class SuperNode extends PrimaryNode {

    public static final TreeNodeParser<SuperNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public SuperNode parse(@NotNull TokenIterator iterator) {
            iterator.next(TokenType.SUPER_KEYWORD);
            return new SuperNode();
        }
    };
}
