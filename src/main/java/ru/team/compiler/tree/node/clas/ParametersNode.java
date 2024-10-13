package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ParametersNode extends TreeNode {

    public static final TreeNodeParser<ParametersNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ParametersNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.OPENING_PARENTHESIS);

            List<Par> parameterNodes = new ArrayList<>();

            if (!iterator.consume(TokenType.CLOSING_PARENTHESIS)) {
                while (true) {
                    IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

                    iterator.next(TokenType.COLON);

                    ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

                    parameterNodes.add(new Par(identifierNode, referenceNode));

                    if (iterator.consume(TokenType.CLOSING_PARENTHESIS)) {
                        break;
                    } else if (!iterator.consume(TokenType.COMMA)) {
                        throw new NodeFormatException("comma/closing parenthesis", NodeFormatException.END_OF_STRING);
                    }
                }
            }

            return new ParametersNode(parameterNodes);
        }
    };

    private final List<Par> pars;

    public ParametersNode(@NotNull List<Par> pars) {
        this.pars = List.copyOf(pars);
    }

    @NotNull
    @Unmodifiable
    public List<Par> pars() {
        return pars;
    }

    public record Par(@NotNull IdentifierNode identifierNode, @NotNull ReferenceNode referenceNode) {

    }
}
