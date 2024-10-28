package ru.team.compiler.tree.node.clas;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            ParametersNode parametersNode = ParametersNode.PARSER.parse(iterator);

            ReferenceNode returnIdentifierNode;
            if (iterator.consume(TokenType.COLON)) {
                returnIdentifierNode = ReferenceNode.PARSER.parse(iterator);
            } else {
                returnIdentifierNode = null;
            }

            BodyNode bodyNode;
            if (!isNative) {
                iterator.next(TokenType.IS_KEYWORD);

                bodyNode = BODY_PARSER.parse(iterator);

                iterator.next(TokenType.END_KEYWORD);
            } else {
                bodyNode = new BodyNode(List.of());
            }

            return new MethodNode(isNative, identifierNode, parametersNode, returnIdentifierNode, bodyNode);
        }
    };

    private final boolean isNative;
    private final IdentifierNode name;
    private final ParametersNode parameters;
    private final ReferenceNode returnType;
    private final BodyNode body;

    public MethodNode(boolean isNative, @NotNull IdentifierNode name, @NotNull ParametersNode parameters,
                      @Nullable ReferenceNode returnType, @NotNull BodyNode body) {
        this.isNative = isNative;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    public boolean isNative() {
        return isNative;
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
    public AnalyzeContext traverse(@NotNull AnalyzeContext context) {
        if (returnType != null && !context.hasClass(returnType)) {
            throw new AnalyzerException("Method '%s.%s' references to unknown type '%s'"
                    .formatted(context.currentPath(), name.value(), returnType.value()));
        }

        AnalyzeContext initialContext = context;

        context = context.concatPath(name.value());
        context = parameters.traverse(context);
        body.traverse(context);

        return initialContext;
    }
}
