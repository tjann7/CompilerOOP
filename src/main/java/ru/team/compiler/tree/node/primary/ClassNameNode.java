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
public final class ClassNameNode extends PrimaryNode {

    public static final TreeNodeParser<ClassNameNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ClassNameNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.IDENTIFIER, "className identifier");
            return new ClassNameNode(token.value());
        }
    };

    private final String value;

    public ClassNameNode(@NotNull String value) {
        this.value = value;
    }

    @NotNull
    public String value() {
        return value;
    }
}
