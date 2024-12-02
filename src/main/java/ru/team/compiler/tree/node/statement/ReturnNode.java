package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.analyzer.AnalyzableMethod;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.attribute.CompilationExecutable;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.tree.node.primary.ThisNode;
import ru.team.compiler.util.Opcodes;

import java.io.DataOutput;
import java.io.IOException;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class ReturnNode extends StatementNode {

    public static final TreeNodeParser<ReturnNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public ReturnNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.RETURN_KEYWORD);

            int index = iterator.index();
            try {
                ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);
                return new ReturnNode(expressionNode);
            } catch (NodeFormatException e) {
                iterator.index(index);
                return new ReturnNode(null);
            }
        }
    };

    private final ExpressionNode expression;

    public ReturnNode(@Nullable ExpressionNode expression) {
        this.expression = expression;
    }

    @Nullable
    public ExpressionNode expression() {
        return expression;
    }

    @Override
    public boolean alwaysReturn() {
        return true;
    }

    @Override
    @NotNull
    public AnalyzeContext analyzeUnsafe(@NotNull AnalyzeContext context) {
        ReferenceNode type = expression != null ? expression.type(context, false) : null;

        AnalyzableMethod currentMethod = context.currentMethod("Return");
        if (((type == null) != (currentMethod.returnType() == null))
                || (type != null && !context.isAssignableFrom(currentMethod.returnType(), type))) {
            throw new AnalyzerException("Return at '%s' is invalid: expected '%s', got '%s'"
                    .formatted(context.currentPath(), prettyType(currentMethod.returnType()), prettyType(type)));
        }

        return context;
    }

    @NotNull
    private String prettyType(@Nullable ReferenceNode type) {
        return type != null ? type.value() : "Void";
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull CompilationExecutable currentExecutable, @NotNull DataOutput dataOutput) throws IOException {
        if (expression == null) {
            dataOutput.writeByte(Opcodes.RETURN);
            return;
        }

        if (expression.idArgs().isEmpty()) {

            if (expression.primary() instanceof ReferenceNode referenceNode) {
                int index = variablePool.getIndex(referenceNode);

                // aload (#X)
                dataOutput.write(Opcodes.aload(constantPool, index));
                dataOutput.writeByte(Opcodes.ARETURN);

                context.incrementStackSize(1); // aload

                return;
            } else if (expression.primary() instanceof ThisNode) {
                // aload_0
                dataOutput.write(Opcodes.ALOAD_0);
                dataOutput.writeByte(Opcodes.ARETURN);

                context.incrementStackSize(1); // aload

                return;
            }

        }

        expression.compile(context, currentClass, constantPool, variablePool, currentExecutable, dataOutput, false);
        dataOutput.writeByte(Opcodes.ARETURN);
    }
}
