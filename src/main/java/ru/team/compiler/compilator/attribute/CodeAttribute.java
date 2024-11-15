package ru.team.compiler.compilator.attribute;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.tree.node.clas.MethodNode;
import ru.team.compiler.tree.node.statement.VariableDeclarationNode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public final class CodeAttribute extends Attribute {

    private final MethodNode methodNode;

    public CodeAttribute(@NotNull ConstantPool constantPool, @NotNull MethodNode methodNode) {
        super(constantPool.getUtf("Code"));

        this.methodNode = methodNode;
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        DataOutput byteDataOutput = new DataOutputStream(byteArrayOutputStream);

        // TODO: calculate real max stack
        int maxStack = 100;

        int locals = methodNode.body().flatStatements().stream()
                .mapToInt(value -> value instanceof VariableDeclarationNode ? 1 : 0)
                .sum();

        int maxLocals = methodNode.parameters().pars().size() + locals;

        byteDataOutput.writeShort(maxStack);
        byteDataOutput.writeShort(maxLocals);

        // Code
        byteDataOutput.writeInt(0);
        // TODO: write code

        // Exceptions
        byteDataOutput.writeShort(0);

        // Attributes
        byteDataOutput.writeShort(0);

        // ---

        dataOutput.writeShort(attributeName.index());

        dataOutput.writeInt(byteArrayOutputStream.size());
        dataOutput.write(byteArrayOutputStream.toByteArray());
    }
}
