package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzableVariable;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.attribute.CompilationExecutable;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class VariableDeclarationNode extends StatementNode {

    public static final TreeNodeParser<VariableDeclarationNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public VariableDeclarationNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.VAR_KEYWORD);

            IdentifierNode identifierNode = IdentifierNode.PARSER.parse(iterator);

            iterator.next(TokenType.COLON);

            ReferenceNode referenceNode = ReferenceNode.PARSER.parse(iterator);

            return new VariableDeclarationNode(identifierNode, referenceNode);
        }
    };

    private final IdentifierNode name;
    private final ReferenceNode type;

    public VariableDeclarationNode(@NotNull IdentifierNode name, @NotNull ReferenceNode type) {
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
    public AnalyzeContext analyzeUnsafe(@NotNull AnalyzeContext context) {
        Map<ReferenceNode, AnalyzableVariable> variables = new HashMap<>(context.variables());

        if (!context.hasClass(type)) {
            throw new AnalyzerException("Variable '%s.%s' references to unknown type '%s'"
                    .formatted(context.currentPath(), name.value(), type.value()));
        }

        ReferenceNode nameReference = name.asReference();
        if (variables.containsKey(nameReference)) {
            throw new AnalyzerException("Variable '%s.%s' is already defined"
                    .formatted(context.currentPath(), name.value()));
        }

        variables.put(nameReference, new AnalyzableVariable(name, type));

        return context.withVariables(variables);
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull CompilationExecutable currentExecutable, @NotNull DataOutput dataOutput) throws IOException {

    }
}
