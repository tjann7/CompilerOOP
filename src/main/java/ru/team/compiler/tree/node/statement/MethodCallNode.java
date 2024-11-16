package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.exception.NodeFormatException;
import ru.team.compiler.token.Token;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.util.Opcodes;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class MethodCallNode extends StatementNode {

    public static final TreeNodeParser<MethodCallNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public MethodCallNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            Token token = iterator.lookup();

            ExpressionNode expression = ExpressionNode.PARSER.parse(iterator);

            List<ExpressionNode.IdArg> idArgs = expression.idArgs();
            if (idArgs.isEmpty() || idArgs.get(idArgs.size() - 1).arguments() == null) {
                throw new NodeFormatException("method call", "field reference", token);
            }

            return new MethodCallNode(expression);
        }
    };

    private final ExpressionNode expression;

    public MethodCallNode(@NotNull ExpressionNode expression) {
        this.expression = expression;
    }

    @NotNull
    public ExpressionNode expression() {
        return expression;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        expression.type(context, true);
        return context;
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull DataOutput dataOutput) throws IOException {
        ReferenceNode type = expression.compile(context, currentClass, constantPool, variablePool, dataOutput, true);

        if (!type.value().equals("<void>")) {
            // pop
            dataOutput.writeByte(Opcodes.POP);
        }
    }
}
