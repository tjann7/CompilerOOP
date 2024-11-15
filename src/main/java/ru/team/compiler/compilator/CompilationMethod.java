package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.attribute.CodeAttribute;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.compilator.constant.Utf8Constant;
import ru.team.compiler.tree.node.clas.MethodNode;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;

public record CompilationMethod(@NotNull Utf8Constant name, @NotNull Utf8Constant descriptor,
                                @NotNull CodeAttribute codeAttribute) {

    @NotNull
    public static CompilationMethod fromNode(@NotNull ConstantPool constantPool, @NotNull MethodNode methodNode) {
        if (methodNode.isNative()) {
            throw new IllegalArgumentException("Cannot convert native MethodNode to CompilationMethod");
        }

        Utf8Constant name = constantPool.getUtf(methodNode.name().value());
        Utf8Constant descriptor = constantPool.getUtf(CompilationUtils.descriptor(methodNode));

        CodeAttribute codeAttribute = new CodeAttribute();

        return new CompilationMethod(name, descriptor, codeAttribute);
    }

    public int accessFlags() {
        return Modifier.PUBLIC;
    }

    public void compile(@NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(accessFlags());
        dataOutput.writeShort(name.index());
        dataOutput.writeShort(descriptor.index());

        // Attributes
        dataOutput.writeShort(1);
        codeAttribute.compile(dataOutput);
    }
}
