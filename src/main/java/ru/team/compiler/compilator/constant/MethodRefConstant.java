package ru.team.compiler.compilator.constant;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.util.Pair;

import java.io.DataOutput;
import java.io.IOException;

public final class MethodRefConstant extends Constant<Pair<ClassConstant, NameAndTypeConstant>> {

    public MethodRefConstant(int index, @NotNull ClassConstant classConstant,
                            @NotNull NameAndTypeConstant nameAndTypeConstant) {
        super(index, Pair.of(classConstant, nameAndTypeConstant));
    }

    @Override
    protected MethodRefConstant withIndex(int index) {
        return new MethodRefConstant(index, value.getLeft(), value.getRight());
    }

    @Override
    public void serialize(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(10);

        ClassConstant classRef = value.getLeft();
        NameAndTypeConstant nameAndType = value.getRight();

        dataOutput.writeShort(classRef.index);
        dataOutput.writeShort(nameAndType.index);
    }
}
