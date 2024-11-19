package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzableClass;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.statement.BodyNode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class MethodNode extends ClassMemberNode {

    public static final TreeNodeParser<BodyNode> BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);

    public static final TreeNodeParser<MethodNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public MethodNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.METHOD_KEYWORD);

            boolean isNative = iterator.consume(TokenType.NATIVE_KEYWORD);
            boolean isAbstract = iterator.consume(TokenType.ABSTRACT_KEYWORD);

            if (isNative && isAbstract) {
                // TODO: throw error
            }

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            ParametersNode parametersNode = ParametersNode.PARSER.parse(iterator);

            ReferenceNode returnIdentifierNode;
            if (iterator.consume(TokenType.COLON)) {
                returnIdentifierNode = ReferenceNode.PARSER.parse(iterator);
            } else {
                returnIdentifierNode = null;
            }

            BodyNode bodyNode;
            if (!isNative && !isAbstract) {
                iterator.next(TokenType.IS_KEYWORD);

                bodyNode = BODY_PARSER.parse(iterator);

                iterator.next(TokenType.END_KEYWORD);
            } else {
                bodyNode = new BodyNode(List.of());

                iterator.next(TokenType.SEMICOLON);
            }

            return new MethodNode(isNative, isAbstract, identifierNode, parametersNode, returnIdentifierNode, bodyNode);
        }
    };

    private final boolean isNative;
    private final boolean isAbstract;
    private final IdentifierNode name;
    private final ParametersNode parameters;
    private final ReferenceNode returnType;
    private final BodyNode body;

    public MethodNode(boolean isNative, boolean isAbstract, @NotNull IdentifierNode name,
                      @NotNull ParametersNode parameters, @Nullable ReferenceNode returnType, @NotNull BodyNode body) {
        this.isNative = isNative;
        this.isAbstract = isAbstract;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    public boolean isNative() {
        return isNative;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @NotNull
    public IdentifierNode name() {
        return name;
    }

    @NotNull
    public ParametersNode parameters() {
        return parameters;
    }

    @Nullable
    public ReferenceNode returnType() {
        return returnType;
    }

    @NotNull
    public BodyNode body() {
        return body;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        AnalyzeContext initialContext = context;

        if (returnType != null && !initialContext.hasClass(returnType)) {
            throw new AnalyzerException("Method '%s.%s' references to unknown type '%s'"
                    .formatted(initialContext.currentPath(), name.value(), returnType.value()));
        }

        context = context.withMethod(this);
        context = parameters.analyze(context);
        body.analyze(context);

        AnalyzableClass analyzableClass = context.currentClass("Method");

        AnalyzableClass parentClass = analyzableClass.findParentClass(context, "Method");
        if (parentClass != null) {
            AnalyzableMethod.Key key = AnalyzableMethod.Key.fromNode(this);

            AnalyzableMethod method = parentClass.methods().get(key);
            if (method != null) {
                if ((method.returnType() == null) != (returnType == null)
                        || returnType != null && !context.isAssignableFrom(method.returnType(), returnType)) {
                    throw new AnalyzerException("Method '%s.%s' overrides method with incorrect return type '%s' (expected '%s')"
                            .formatted(initialContext.currentPath(), name.value(),
                                    prettyType(returnType), prettyType(method.returnType())));
                }
            }
        }

        if (!isNative && !isAbstract && returnType != null && !body.alwaysReturn()) {
            throw new AnalyzerException("Method '%s.%s' does not always return"
                    .formatted(initialContext.currentPath(), name.value()));
        }

        return initialContext;
    }

    @NotNull
    private String prettyType(@Nullable ReferenceNode type) {
        return type != null ? type.value() : "Void";
    }

    @Override
    @NotNull
    public MethodNode optimize() {
        return new MethodNode(
                isNative,
                isAbstract,
                name,
                parameters,
                returnType,
                body.optimize()
        );
    }
}
