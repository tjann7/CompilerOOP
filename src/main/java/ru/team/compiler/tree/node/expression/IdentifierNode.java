package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.primary.ReferenceNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class IdentifierNode extends TreeNode {

    public static final TreeNodeParser<IdentifierNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public IdentifierNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.IDENTIFIER, "identifier");
            return new IdentifierNode(token.value());
        }
    };

    private final String value;

    public IdentifierNode(@NotNull String value) {
        this.value = value;
    }

    @NotNull
    public String value() {
        return value;
    }

    @NotNull
    public ReferenceNode asReference() {
        return new ReferenceNode(value);
    }
}
