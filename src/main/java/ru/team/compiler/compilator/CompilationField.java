package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.compilator.constant.Utf8Constant;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.FieldNode;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;

public record CompilationField(@NotNull Utf8Constant name, @NotNull Utf8Constant descriptor) {

    @NotNull
    public static CompilationField fromNode(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                                            @NotNull FieldNode fieldNode) {
        Utf8Constant name = constantPool.getUtf(fieldNode.name().value());
        Utf8Constant descriptor = constantPool.getUtf(CompilationUtils.descriptor(fieldNode));

        return new CompilationField(name, descriptor);
    }

    public int accessFlags() {
        return Modifier.PUBLIC;
    }

    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(accessFlags());
        dataOutput.writeShort(name.index());
        dataOutput.writeShort(descriptor.index());

        // Attributes
        dataOutput.writeShort(0);
    }
}
