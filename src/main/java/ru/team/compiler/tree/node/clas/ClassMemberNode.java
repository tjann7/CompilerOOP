package ru.team.compiler.tree.node.clas;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;

public abstract sealed class ClassMemberNode extends TreeNode permits FieldNode, MethodNode, ConstructorNode {

    public static final TreeNodeParser<ClassMemberNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ClassMemberNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            Token token = iterator.lookup();

            return switch (token.type()) {
                case VAR_KEYWORD -> FieldNode.PARSER.parse(iterator);
                case METHOD_KEYWORD -> MethodNode.PARSER.parse(iterator);
                case THIS_KEYWORD -> ConstructorNode.PARSER.parse(iterator);
                default -> throw new NodeFormatException("var/method/this", token);
            };
        }
    };
}
