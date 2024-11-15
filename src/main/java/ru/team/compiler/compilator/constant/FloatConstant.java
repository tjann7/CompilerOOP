package ru.team.compiler.compilator.constant;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

public final class FloatConstant extends Constant<Float> {

    public FloatConstant(int index, float value) {
        super(index, value);
    }

    @Override
    protected Constant<Float> withIndex(int index) {
        return new FloatConstant(index, value);
    }

    @Override
    public void serialize(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(4);
        dataOutput.writeFloat(value);
    }
}
