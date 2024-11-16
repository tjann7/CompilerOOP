package ru.team.compiler.tree.node.statement;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import ru.team.compiler.tree.node.expression.ArgumentsNode;
import ru.team.compiler.tree.node.expression.ExpressionNode;
import ru.team.compiler.tree.node.expression.IdentifierNode;
import ru.team.compiler.tree.node.primary.BooleanLiteralNode;
import ru.team.compiler.tree.node.primary.ReferenceNode;
import ru.team.compiler.util.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@ToString
public final class IfNode extends StatementNode {

    public static final TreeNodeParser<BodyNode> THEN_BODY_PARSER = BodyNode.parser(
            TokenType.ELSE_KEYWORD, TokenType.END_KEYWORD
    );
    public static final TreeNodeParser<BodyNode> ELSE_BODY_PARSER = BodyNode.parser(TokenType.END_KEYWORD);

    public static final TreeNodeParser<IfNode> PARSER = new TreeNodeParser<>() {
        @Override
        @NotNull
        public IfNode parse(@NotNull TokenIterator iterator) throws CompilerException {
            iterator.next(TokenType.IF_KEYWORD);

            ExpressionNode expressionNode = ExpressionNode.PARSER.parse(iterator);

            iterator.next(TokenType.THEN_KEYWORD);

            BodyNode thenBodyNode = THEN_BODY_PARSER.parse(iterator);

            BodyNode elseBodyNode;

            if (iterator.consume(TokenType.ELSE_KEYWORD)) {
                elseBodyNode = ELSE_BODY_PARSER.parse(iterator);
            } else {
                elseBodyNode = new BodyNode(List.of());
            }

            iterator.next(TokenType.END_KEYWORD);

            return new IfNode(expressionNode, thenBodyNode, elseBodyNode);
        }
    };

    private final ExpressionNode condition;
    private final BodyNode thenBody;
    private final BodyNode elseBody;

    public IfNode(@NotNull ExpressionNode condition, @NotNull BodyNode thenBody,
                  @Nullable BodyNode elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    @NotNull
    public ExpressionNode condition() {
        return condition;
    }

    @NotNull
    public BodyNode thenBody() {
        return thenBody;
    }

    @Nullable
    public BodyNode elseBody() {
        return elseBody;
    }

    @Override
    @NotNull
    public AnalyzeContext analyze(@NotNull AnalyzeContext context) {
        ReferenceNode type = condition.type(context, false);

        if (!type.value().equals("Boolean")) {
            throw new AnalyzerException("If condition at '%s' is invalid: expected 'Boolean' type, got '%s'"
                    .formatted(context.currentPath(), type.value()));
        }

        thenBody.analyze(context);
        if (elseBody != null) {
            elseBody.analyze(context);
        }
        return context;
    }

    @Override
    public boolean alwaysReturn() {
        return thenBody.alwaysReturn() && (elseBody != null && elseBody().alwaysReturn());
    }

    @Override
    @NotNull
    public List<StatementNode> optimize() {
        if (condition.primary() instanceof BooleanLiteralNode booleanLiteralNode && condition.idArgs().isEmpty()) {
            if (booleanLiteralNode.value()) {
                return thenBody.optimize().statements();
            } else {
                return elseBody != null ? elseBody.optimize().statements() : List.of();
            }
        }

        BodyNode optimizedThenBody = thenBody.optimize();
        BodyNode optimizedElseBody = elseBody != null ? elseBody.optimize() : null;

        if (optimizedThenBody.statements().isEmpty()) {
            optimizedThenBody = null;
        }

        if (optimizedElseBody != null && optimizedElseBody.statements().isEmpty()) {
            optimizedElseBody = null;
        }

        if (optimizedThenBody == null) {
            if (optimizedElseBody == null) {
                MethodCallNode methodCall = condition.asMethodCall();
                return methodCall != null ? List.of(methodCall) : List.of();
            } else {
                return List.of(new IfNode(
                        condition.withIdArgs(List.of(
                                new ExpressionNode.IdArg(
                                        new IdentifierNode("not"), new ArgumentsNode(List.of())))),
                        optimizedElseBody,
                        null));
            }
        }

        if (thenBody.equals(elseBody)) {
            MethodCallNode methodCall = condition.asMethodCall();
            if (methodCall != null) {
                List<StatementNode> optimized = new ArrayList<>(1 + thenBody.statements().size());

                optimized.add(methodCall);
                optimized.addAll(thenBody.statements());

                return optimized;
            } else {
                return thenBody.statements();
            }
        }

        return List.of(new IfNode(condition, optimizedThenBody, optimizedElseBody));
    }

    @Override
    public void compile(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                        @NotNull ConstantPool constantPool, @NotNull CodeAttribute.VariablePool variablePool,
                        @NotNull DataOutput dataOutput) throws IOException {
        condition.compile(context, currentClass, constantPool, variablePool, dataOutput, false);

        // olang.Boolean -> boolean primitive
        // invokevirtual (#Boolean.java$value()Z)
        dataOutput.writeByte(Opcodes.INVOKEVIRTUAL);
        MethodRefConstant oMethod = CompilationUtils.oMethod(constantPool,
                "Boolean", "java$value", "()Z");
        dataOutput.writeShort(oMethod.index());

        // Firstly, compile then body because we need to know else body offset for IFEQ opcode
        byte[] compiledThenBody = compileBodyNode(context, currentClass, constantPool, variablePool, thenBody);

        boolean hasElseBody = elseBody != null && !elseBody.statements().isEmpty();

        // Secondly, define IFEQ with known offset for else body
        // ifeq (#X)
        int offset = 3 + (hasElseBody ? 3 : 0) + compiledThenBody.length;

        dataOutput.writeByte(Opcodes.IFEQ);
        dataOutput.writeShort(offset);

        dataOutput.write(compiledThenBody);

        if (hasElseBody) {
            // Firstly, compile else body because we need to know offset for GOTO opcode
            byte[] compiledElseBody = compileBodyNode(context, currentClass, constantPool, variablePool, elseBody);

            // Secondly, define GOTO with known size of else body
            // goto (#X)
            offset = 3 + compiledElseBody.length;

            dataOutput.writeByte(Opcodes.GOTO);
            dataOutput.writeShort(offset);

            dataOutput.write(compiledElseBody);
        }
    }

    private byte @NotNull [] compileBodyNode(@NotNull CompilationContext context, @NotNull ClassNode currentClass,
                                             @NotNull ConstantPool constantPool,
                                             @NotNull CodeAttribute.VariablePool variablePool,
                                             @NotNull BodyNode bodyNode) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutputStream byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        for (StatementNode statementNode : bodyNode.statements()) {
            statementNode.compile(context, currentClass, constantPool, variablePool, byteDataOutput);
        }

        return byteArrayOutputStream.toByteArray();
    }
}
