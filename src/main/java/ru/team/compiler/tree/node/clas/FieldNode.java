package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class FieldNode extends ClassMemberNode {

    public static final TreeNodeParser<FieldNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public FieldNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.VAR_KEYWORD);

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            iterator.next(TokenType.COLON);

            ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

            return new FieldNode(identifierNode, referenceNode);
        }
    };

    private final IdentifierNode identifierNode;
    private final ReferenceNode referenceNode;

    public FieldNode(@NotNull IdentifierNode identifierNode, @NotNull ReferenceNode referenceNode) {
        this.identifierNode = identifierNode;
        this.referenceNode = referenceNode;
    }

    @NotNull
    public IdentifierNode identifierNode() {
        return identifierNode;
    }

    @NotNull
    public ReferenceNode referenceNode() {
        return referenceNode;
    }

}
