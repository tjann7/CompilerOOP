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
            Token token = iterator.lookup("integer/real/boolean/this/className identifier");

            return switch (token.type()) {
                case INTEGER_LITERAL -> IntegerLiteralNode.PARSER.parse(iterator);
                case REAL_LITERAL -> RealLiteralNode.PARSER.parse(iterator);
                case BOOLEAN_LITERAL -> BooleanLiteralNode.PARSER.parse(iterator);
                case THIS_KEYWORD -> ThisNode.PARSER.parse(iterator);
                case IDENTIFIER -> ClassNameNode.PARSER.parse(iterator);
                default -> throw new NodeFormatException("integer/real/boolean/this/className identifier", token);
            };
        }
    };
}
