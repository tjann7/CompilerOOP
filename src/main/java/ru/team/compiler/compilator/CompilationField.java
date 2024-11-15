package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.constant.ConstantPool;
import ru.team.compiler.compilator.constant.Utf8Constant;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;

public record CompilationField(@NotNull Utf8Constant name, @NotNull Utf8Constant descriptor) {

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
