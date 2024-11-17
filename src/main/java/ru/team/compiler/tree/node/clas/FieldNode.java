package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
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

            iterator.next(TokenType.SEMICOLON);

            return new FieldNode(identifierNode, referenceNode);
        }
    };

    private final IdentifierNode name;
    private final ReferenceNode type;

    public FieldNode(@NotNull IdentifierNode name, @NotNull ReferenceNode type) {
        this.name = name;
        this.type = type;
    }

    @NotNull
    public IdentifierNode name() {
        return name;
    }

    @NotNull
    public ReferenceNode type() {
        return type;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        if (!context.hasClass(type)) {
            throw new AnalyzerException("Field '%s.%s' references to unknown type '%s'"
                    .formatted(context.currentPath(), name.value(), type.value()));
        }

        return context;
    }
}
