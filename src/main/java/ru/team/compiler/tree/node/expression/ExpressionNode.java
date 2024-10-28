package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ParametersNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ExpressionNode extends TreeNode {

    public static final TreeNodeParser<ExpressionNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ExpressionNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            PrimaryNode primary = PrimaryNode.PARSER.parse(iterator);

            List<IdArg> idArgs = new ArrayList<>();
            while (iterator.hasNext()) {
                if (iterator.consume(TokenType.DOT)) {
                    IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

                    ArgumentsNode argumentsNode;

                    if (iterator.lookup(TokenType.OPENING_PARENTHESIS)) {
                        argumentsNode = ArgumentsNode.PARSER.parse(iterator);
                    } else {
                        argumentsNode = null;
                    }

                    idArgs.add(new IdArg(identifierNode, argumentsNode));
                } else {
                    break;
                }
            }

            return new ExpressionNode(primary, idArgs);
        }
    };

    private final PrimaryNode primary;
    private final List<IdArg> idArgs;

    public ExpressionNode(@NotNull PrimaryNode primary, @NotNull List<IdArg> idArgs) {
        this.primary = primary;
        this.idArgs = List.copyOf(idArgs);
    }

    @NotNull
    public PrimaryNode primary() {
        return primary;
    }

    @NotNull
    @Unmodifiable
    public List<IdArg> idArgs() {
        return idArgs;
    }

    public record IdArg(@NotNull IdentifierNode name, @Nullable ArgumentsNode arguments) {

    }

    @Override
    @NotNull
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        List<ParametersNode.Par> pars = new ArrayList<>();

        if (primary instanceof ReferenceNode referenceNode) {
            if (!context.classes().containsKey(referenceNode) && !context.variables().containsKey(referenceNode)) {
                throw new AnalyzerException("Expression in '%s' references to unknown type '%s'"
                        .formatted(context.currentPath(), referenceNode.value()));
            } else if (context.variables().containsKey(referenceNode)) {
                AnalyzableClass classContext = context.classes()
                        .get(context.variables().get(referenceNode));
                for (IdArg i : idArgs) {
                    if (!context.variables().containsKey(i.name.value()))
                        throw new AnalyzerException();
                    pars.add(new ParametersNode.Par
                            (i.name, new ReferenceNode(context.variables().get(i.name))));
                }

                ParametersNode params = new ParametersNode(pars);
                AnalyzableMethod check = new AnalyzableMethod
                        (new IdentifierNode(referenceNode.value()), params);

                if (!classContext.methods().containsKey(check)) {
                    throw new AnalyzerException();
                }
                // TODO: add check for each IdArg that it references to correct classes
            } else {

            }
        }

        return context;
    }
}
