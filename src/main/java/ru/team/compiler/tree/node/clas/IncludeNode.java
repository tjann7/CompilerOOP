package ru.team.compiler.tree.node.clas;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.primary.ReferenceNode;

public final class IncludeNode extends TreeNode {

    public static final TreeNodeParser<IncludeNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public IncludeNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.INCLUDE_KEYWORD);

            ReferenceNode fileName = ReferenceNode.PARSER.parse(iterator);

            iterator.next(TokenType.SEMICOLON);

            return new IncludeNode(fileName);
        }
    };

    private final ReferenceNode fileName;

    public IncludeNode(@NotNull ReferenceNode fileName) {
        this.fileName = fileName;
    }

    @NotNull
    public ReferenceNode fileName() {
        return fileName;
    }
}
