package ru.team.compiler.tree.node.expression;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableField;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNode;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.IntegerLiteralNode;
import ru.team.compiler.tree.node.primary.PrimaryNode;
import ru.team.compiler.tree.node.primary.RealLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.ThisNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        ReferenceNode currentType;

        // var a : A
        // a := A(1).

        int shift = 0;

        if (primary instanceof ReferenceNode referenceNode) {
            if (context.hasClass(referenceNode)) {
                if (idArgs.isEmpty()) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to type"
                            .formatted(context.currentPath()));
                }

                IdArg idArg = idArgs.get(0);
                if (!idArg.name.value().equals("<init>")) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to static"
                            .formatted(context.currentPath()));
                }

                if (idArg.arguments != null) {
                    throw new AnalyzerException("Expression in '%s' is invalid: no arguments for constructor"
                            .formatted(context.currentPath()));
                }

                // TODO: check argument correctness

                shift = 1;

                currentType = referenceNode;
            } else if (context.hasVariable(referenceNode)) {
                AnalyzableVariable variable = context.variables().get(referenceNode);
                currentType = variable.type();
            } else {
                throw new AnalyzerException("Expression in '%s' references to unknown type or variable '%s'"
                        .formatted(context.currentPath(), referenceNode.value()));
            }
        } else if (primary instanceof BooleanLiteralNode) {
            currentType = new ReferenceNode("Boolean");
        } else if (primary instanceof IntegerLiteralNode) {
            currentType = new ReferenceNode("Integer");
        } else if (primary instanceof RealLiteralNode) {
            currentType = new ReferenceNode("Real");
        } else if (primary instanceof ThisNode) {
            AnalyzableClass currentClass = context.currentClass();
            if (currentClass == null) {
                throw new AnalyzerException("Expression in '%s' is invalid: reference to this outside of the class"
                        .formatted(context.currentPath()));
            }

            currentType = currentClass.name().asReference();
        } else {
            throw new AnalyzerException("Expression in '%s' is invalid: '%s' is not supported"
                    .formatted(context.currentPath(), primary));
        }

        for (int i = shift; i < idArgs.size(); i++) {
            AnalyzableClass analyzableClass = context.classes().get(currentType);

            IdArg idArg = idArgs.get(i);

            if (idArg.arguments == null) {
                AnalyzableField field;

                AnalyzableField.Key key = new AnalyzableField.Key(idArg.name);
                AnalyzableClass currentClass = analyzableClass;
                while (true) {
                    field = analyzableClass.fields().get(key);
                    if (field != null) {
                        break;
                    }

                    currentClass = parentClass(context, currentClass);
                    if (currentClass == null) {
                        break;
                    }
                }

                if (field == null) {
                    throw new AnalyzerException("Expression in '%s' is invalid: reference to unknown field '%s' in type '%s'"
                            .formatted(context.currentPath(), idArg.name, currentType.value()));
                }
            } else {
                AnalyzableMethod method = null;

                AnalyzableClass currentClass = analyzableClass;
                while (true) {
                    List<AnalyzableMethod> methods = analyzableClass.methods().values()
                            .stream()
                            .filter(m -> m.name().equals(idArg.name))
                            .collect(Collectors.toList());

                    for (AnalyzableMethod analyzableMethod : methods) {
                        // TODO: check that arguments are fit and place condition instead "true"
                        // This such case must be supported:
                        // method a(a: A)
                        // B extends A
                        // =>
                        // a(A(1)) & a(B(1))
                        if (true) {
                            method = analyzableMethod;
                            break;
                        }
                    }

                    if (method != null) {
                        break;
                    }

                    currentClass = parentClass(context, currentClass);
                    if (currentClass == null) {
                        break;
                    }
                }
            }
        }

        return context;
    }

    @Nullable
    private AnalyzableClass parentClass(@NotNull AnalyzeContext context, @NotNull AnalyzableClass currentClass) {
        String name = currentClass.name().value();
        if (name.equals("Any") || name.equals("")) {
            return null;
        }

        AnalyzableClass parentClass = context.classes().get(currentClass.parentClass());
        if (parentClass == null) {
            throw new AnalyzerException("Expression in '%s' is invalid: class '%s' extends unknown '%s'"
                    .formatted(context.currentPath(), currentClass.name().value(),
                            currentClass.parentClass().value()));
        }

        return parentClass;
    }
}
