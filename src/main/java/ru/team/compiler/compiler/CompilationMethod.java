package ru.team.compiler.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.compiler.attribute.CodeAttribute;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.Utf8Constant;
import ru.team.compiler.tree.node.clas.ClassNode;
import ru.team.compiler.tree.node.clas.ConstructorNode;
import ru.team.compiler.tree.node.clas.MethodNode;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.stream.Collectors;

public record CompilationMethod(@NotNull Utf8Constant name, @NotNull Utf8Constant descriptor,
                                @Nullable CodeAttribute codeAttribute, int modifiers) {

    @NotNull
    public static CompilationMethod fromNode(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                                             @NotNull MethodNode methodNode) {
        return fromNode(constantPool, classNode, methodNode, 0);
    }

    @NotNull
    public static CompilationMethod fromNode(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                                             @NotNull MethodNode methodNode, int modifiers) {
        if (methodNode.isNative()) {
            throw new IllegalArgumentException("Class '%s' has native method '%s(%s)', so it cannot be compiled"
                    .formatted(
                            classNode.name().value(),
                            methodNode.name().value(), methodNode.parameters().pars().stream()
                                    .map(par -> par.type().value())
                                    .collect(Collectors.joining(", "))));
        }

        if (methodNode.isAbstract() && !classNode.isAbstract()) {
            throw new IllegalArgumentException("Class '%s' must be abstract, because method '%s(%s)' is abstract"
                    .formatted(
                            classNode.name().value(),
                            methodNode.name().value(), methodNode.parameters().pars().stream()
                                    .map(par -> par.type().value())
                                    .collect(Collectors.joining(", "))));
        }

        Utf8Constant name = constantPool.getUtf(methodNode.name().value());
        Utf8Constant descriptor = constantPool.getUtf(CompilationUtils.descriptor(methodNode));

        CodeAttribute codeAttribute = methodNode.isAbstract()
                ? null
                : new CodeAttribute(constantPool, classNode, methodNode);

        return new CompilationMethod(name, descriptor, codeAttribute, modifiers);
    }

    @NotNull
    public static CompilationMethod fromNode(@NotNull ConstantPool constantPool, @NotNull ClassNode classNode,
                                             @NotNull ConstructorNode constructorNode) {
        if (constructorNode.isNative()) {
            throw new IllegalArgumentException("Cannot convert native ConstructorNode to CompilationMethod");
        }

        Utf8Constant name = constantPool.getUtf("<init>");
        Utf8Constant descriptor = constantPool.getUtf(CompilationUtils.descriptor(constructorNode));

        CodeAttribute codeAttribute = new CodeAttribute(constantPool, classNode, constructorNode);

        return new CompilationMethod(name, descriptor, codeAttribute, 0);
    }

    public int accessFlags() {
        return Modifier.PUBLIC | (codeAttribute == null ? Modifier.ABSTRACT : 0) | modifiers;
    }

    public void compile(@NotNull CompilationContext context, @NotNull ConstantPool constantPool,
                        @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(accessFlags());
        dataOutput.writeShort(name.index());
        dataOutput.writeShort(descriptor.index());

        // Attributes
        if (codeAttribute != null) {
            dataOutput.writeShort(1);
            codeAttribute.compile(context, constantPool, dataOutput);
        } else {
            dataOutput.writeShort(0);
        }
    }
}
