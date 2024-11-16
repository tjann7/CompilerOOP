package ru.team.compiler.compiler.constant;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

public final class FloatConstant extends Constant<Float> {

    public FloatConstant(short index, float value) {
        super(index, value);
    }

    @Override
    protected Constant<Float> withIndex(short index) {
        return new FloatConstant(index, value);
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(4);
        dataOutput.writeFloat(value);
    }
}
