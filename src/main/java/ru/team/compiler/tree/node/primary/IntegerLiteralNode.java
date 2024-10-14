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
public final class IntegerLiteralNode extends PrimaryNode {

    public static final TreeNodeParser<IntegerLiteralNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public IntegerLiteralNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next(TokenType.INTEGER_LITERAL);
            String value = token.value();

            try {
                return new IntegerLiteralNode(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new NodeFormatException("integer", value, token);
            }
        }
    };

    private final int value;

}
