package ru.team.compiler.compiler.constant;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

public final class ClassConstant extends Constant<Utf8Constant> {

    public ClassConstant(short index, @NotNull Utf8Constant value) {
        super(index, value);
    }

    @Override
    protected ClassConstant withIndex(short index) {
        return new ClassConstant(index, value);
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(7);

        dataOutput.writeShort(value.index);
    }
}
