package ru.team.compiler.tree.node.primary;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

public abstract sealed class PrimaryNode extends TreeNode permits IntegerLiteralNode, RealLiteralNode,
                                                                  BooleanLiteralNode, ThisNode, ClassNameNode {

    public static final TreeNodeParser<PrimaryNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public PrimaryNode parse(@NotNull TokenIterator iterator) {
            Token token = iterator.next("integer/real/boolean/this/className identifier");

            return switch (token.type()) {
                case INTEGER_LITERAL -> IntegerLiteralNode.PARSER.parse(token);
                case REAL_LITERAL -> RealLiteralNode.PARSER.parse(token);
                case BOOLEAN_LITERAL -> BooleanLiteralNode.PARSER.parse(token);
                case THIS_KEYWORD -> ThisNode.PARSER.parse(token);
                case IDENTIFIER -> ClassNameNode.PARSER.parse(token);
                default -> throw new NodeFormatException("integer/real/boolean/this/className identifier", token);
            };
        }
    };
}
