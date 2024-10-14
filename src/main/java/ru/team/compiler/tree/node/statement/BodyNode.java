package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class BodyNode extends TreeNode {

    private final List<StatementNode> statementNodes;

    public BodyNode(@NotNull List<StatementNode> statementNodes) {
        this.statementNodes = List.copyOf(statementNodes);
    }

    @NotNull
    @Unmodifiable
    public List<StatementNode> statementNodes() {
        return statementNodes;
    }

    @NotNull
    public static TreeNodeParser<BodyNode> parser(@NotNull TokenType @NotNull ... endTypes) {
        return parser(Set.of(endTypes));
    }

    @NotNull
    public static TreeNodeParser<BodyNode> parser(@NotNull Set<TokenType> endTypes) {
        return parser(endTypes, endTypes.stream().map(TokenType::name)
                .map(s -> s.toLowerCase().replace("_", " "))
                .collect(Collectors.joining("/")));
    }

    @NotNull
    public static TreeNodeParser<BodyNode> parser(@NotNull Set<TokenType> endTypes, @NotNull String expectedMessage) {
        return new TreeNodeParser<>() {
            @Override
            @NotNull
            public BodyNode parse(@NotNull TokenIterator iterator) throws CompilerException {
                List<StatementNode> statementNodes = new ArrayList<>();

                while (iterator.hasNext()) {
                    Token token = iterator.lookup();
                    if (endTypes.contains(token.type())) {
                        return new BodyNode(statementNodes);
                    }

                    if (!statementNodes.isEmpty()) {
                        while (iterator.consume(TokenType.SEMICOLON)) {
                        }

                        token = iterator.lookup();
                        if (endTypes.contains(token.type())) {
                            return new BodyNode(statementNodes);
                        }
                    }

                    StatementNode statementNode = StatementNode.PARSER.parse(iterator);
                    statementNodes.add(statementNode);
                }

                throw new NodeFormatException(expectedMessage, NodeFormatException.END_OF_STRING, iterator.lastToken());
            }
        };
    }
}
