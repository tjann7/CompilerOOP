package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.team.compiler.compilator.constant.ClassConstant;
import ru.team.compiler.compilator.constant.ConstantPool;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

public record ClassFile(int minorVersion, int majorVersion, @NotNull ConstantPool constantPool,
                        @NotNull ClassConstant thisClass, @Nullable ClassConstant superClass,
                        @NotNull List<CompilationField> fields, @NotNull List<CompilationMethod> methods) {

    public int accessFlags() {
        return Modifier.PUBLIC;
    }

    public void compile(@NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(0xCAFEBABE);

        dataOutput.writeShort(52); // Java 8
        dataOutput.writeShort(0);

        constantPool.compile(dataOutput);

        dataOutput.writeShort(accessFlags());

        dataOutput.writeShort(thisClass.index());
        dataOutput.writeShort(superClass != null ? superClass.index() : 0);

        // Interfaces
        dataOutput.writeShort(0);

        // Fields
        dataOutput.writeShort(fields.size());

        // Methods
        dataOutput.writeShort(methods.size());

        // Attributes
        dataOutput.writeShort(0);
    }
}
