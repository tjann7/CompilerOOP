package ru.team.compiler.compilator.constant;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

public final class IntegerConstant extends Constant<Integer> {

    public IntegerConstant(int index, int value) {
        super(index, value);
    }

    @Override
    protected IntegerConstant withIndex(int index) {
        return new IntegerConstant(index, value);
    }

    @Override
    public void serialize(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(3);
        dataOutput.writeInt(value);
    }
}
