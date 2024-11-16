package ru.team.compiler.compiler.constant;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.util.Pair;

import java.io.DataOutput;
import java.io.IOException;

public final class FieldRefConstant extends Constant<Pair<ClassConstant, NameAndTypeConstant>> {

    public FieldRefConstant(short index, @NotNull ClassConstant classConstant,
                            @NotNull NameAndTypeConstant nameAndTypeConstant) {
        super(index, Pair.of(classConstant, nameAndTypeConstant));
    }

    @Override
    protected FieldRefConstant withIndex(short index) {
        return new FieldRefConstant(index, value.getLeft(), value.getRight());
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(9);

        ClassConstant classRef = value.getLeft();
        NameAndTypeConstant nameAndType = value.getRight();

        dataOutput.writeShort(classRef.index);
        dataOutput.writeShort(nameAndType.index);
    }
}
