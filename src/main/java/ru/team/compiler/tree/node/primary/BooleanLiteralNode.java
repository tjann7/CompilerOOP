package ru.team.compiler.tree.node.primary;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;

import java.util.Map;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
@ToString
public final class BooleanLiteralNode extends PrimaryNode {

    private static final Map<String, Boolean> VALUES = Map.of(
            "true", Boolean.TRUE,
            "false", Boolean.FALSE
    );

    public static final TreeNodeParser<BooleanLiteralNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public BooleanLiteralNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.BOOLEAN_LITERAL);

            Boolean bool = VALUES.get(token.value());
            if (bool == null) {
                throw new NodeFormatException("boolean", token.value(), token);
            }

            return new BooleanLiteralNode(bool);
        }
    };

    private final boolean value;
}
