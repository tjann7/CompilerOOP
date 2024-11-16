package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.CompilationUtils;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.MethodRefConstant;
import ru.team.compiler.exception.AnalyzerException;
import ru.team.compiler.exception.CompilerException;
import ru.team.compiler.token.TokenIterator;
import ru.team.compiler.token.TokenType;
import ru.team.compiler.tree.node.TreeNodeParser;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.util.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class WhileLoopNode extends StatementNode {

    public static final TreeNodeParser<BodyNode> BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);
    public static final TreeNodeParser<WhileLoopNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public WhileLoopNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.WHILE_KEYWORD);

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            iterator.next(TokenType.LOOP_KEYWORD);

            BodyNode bodyNode = BODY_PARSER.parse(iterator);

            iterator.next(TokenType.END_KEYWORD);

            return new WhileLoopNode(expressionNode, bodyNode);
        }
    };

    private final ExpressionNode condition;
    private final BodyNode body;

    public WhileLoopNode(@NotNull ExpressionNode condition, @NotNull BodyNode body) {
        this.condition = condition;
        this.body = body;
    }

    @NotNull
    public ExpressionNode condition() {
        return condition;
    }

    @NotNull
    public BodyNode body() {
        return body;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        ReferenceNode type = condition.type(context, false);

        if (!type.value().equals("Boolean")) {
            throw new AnalyzerException("While condition at '%s' is invalid: expected 'Boolean' type, got '%s'"
                    .formatted(context.currentPath(), type.value()));
        }

        return body.analyze(context);
    }

    @Override
    public boolean alwaysReturn() {
        return body.alwaysReturn();
    }

    @Override
    @NotNull
    public List<StatementNode> optimize() {
        if (condition.primary() instanceof BooleanLiteralNode booleanLiteralNode
                && condition.idArgs().isEmpty()
                && !booleanLiteralNode.value()) {
            return List.of();
        }

        return List.of(new WhileLoopNode(condition, body.optimize()));
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull DataOutput dataOutput) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        // Firstly, compile condition and body because we need to know offset for IFEQ and GOTO opcodes
        condition.compile(context, currentClass, constantPool, variablePool, byteDataOutput, false);

        // olang.Boolean -> boolean primitive
        // invokevirtual (#Boolean.java$value()Z)
        byteDataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
        MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool,
                "Boolean", "java$value", "()Z");
        byteDataOutput.writeShort(oMethod.index());

        byte[] compiledBody = compileBodyNode(context, currentClass, constantPool, variablePool);

        // ifeq (#X)
        int offset = 3 + compiledBody.length + 3;

        byteDataOutput.writeByte(Opcodes.IFEQ);
        byteDataOutput.writeShort(offset);

        byteDataOutput.write(compiledBody);

        // Secondly, define GOTO with known offset
        offset = -byteDataOutput.size();

        byteDataOutput.writeByte(Opcodes.GOTO);
        byteDataOutput.writeShort(offset);

        dataOutput.write(byteArrayOutputStream.toByteArray());
    }

    private byte @NotNull [] compileBodyNode(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                                             @NotNull ConstantPool constantPool,
                                             @NotNull CodeAttribute.VariablePool variablePool) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        for (StatementNode statementNode : body.statements()) {
            statementNode.compile(context, currentClass, constantPool, variablePool, byteDataOutput);
        }

        return byteArrayOutputStream.toByteArray();
    }
}
