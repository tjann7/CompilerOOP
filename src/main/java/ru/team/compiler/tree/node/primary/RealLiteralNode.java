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

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
@ToString
public final class RealLiteralNode extends PrimaryNode {

    public static final TreeNodeParser<RealLiteralNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public RealLiteralNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.REAL_LITERAL);
            String value = token.value();

            try {
                return new RealLiteralNode(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                throw new NodeFormatException("double", value, token);
            }
        }
    };

    private final double value;
}
