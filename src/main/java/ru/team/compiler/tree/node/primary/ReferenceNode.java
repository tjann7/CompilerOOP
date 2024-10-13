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
public final class ReferenceNode extends PrimaryNode {

    public static final TreeNodeParser<ReferenceNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ReferenceNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.IDENTIFIER, "reference identifier");
            return new ReferenceNode(token.value());
        }
    };

    private final String value;

    public ReferenceNode(@NotNull String value) {
        this.value = value;
    }

    @NotNull
    public String value() {
        return value;
    }
}
