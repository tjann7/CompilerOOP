package ru.team.compiler.compiler.constant;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.util.Pair;

import java.io.DataOutput;
import java.io.IOException;

public final class NameAndTypeConstant extends Constant<Pair<Utf8Constant, Utf8Constant>> {

    public NameAndTypeConstant(short index, @NotNull Utf8Constant name, @NotNull Utf8Constant descriptor) {
        super(index, Pair.of(name, descriptor));
    }

    @Override
    protected NameAndTypeConstant withIndex(short index) {
        return new NameAndTypeConstant(index, value.getLeft(), value.getRight());
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(12);
        dataOutput.writeShort(value.getLeft().index);
        dataOutput.writeShort(value.getRight().index);
    }
}
