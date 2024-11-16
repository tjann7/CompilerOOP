package ru.team.compiler.compilator.constant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.team.compiler.util.Unsigned;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConstantPool {

    private final List<Constant<?>> constants = new ArrayList<>();
    private final Map<Constant<?>, Short> constantToIndex = new HashMap<>();

    @NotNull
    public ClassConstant getClass(@NotNull Utf8Constant constant) {
        return getConstant(new ClassConstant((short) -1, constant));
    }

    @NotNull
    public FieldRefConstant getFieldRef(@NotNull ClassConstant classConstant,
                                        @NotNull NameAndTypeConstant nameAndTypeConstant) {
        return getConstant(new FieldRefConstant((short) -1, classConstant, nameAndTypeConstant));
    }

    @NotNull
    public MethodRefConstant getMethodRef(@NotNull ClassConstant classConstant,
                                          @NotNull NameAndTypeConstant nameAndTypeConstant) {
        return getConstant(new MethodRefConstant((short) -1, classConstant, nameAndTypeConstant));
    }

    @NotNull
    public IntegerConstant getInt(int i) {
        return getConstant(new IntegerConstant((short) -1, i));
    }

    @NotNull
    public FloatConstant getFloat(float f) {
        return getConstant(new FloatConstant((short) -1, f));
    }

    @NotNull
    public Utf8Constant getUtf(@NotNull String string) {
        return getConstant(new Utf8Constant((short) -1, string));
    }

    @NotNull
    public NameAndTypeConstant getNameAndType(@NotNull Utf8Constant name, @NotNull Utf8Constant descriptor) {
        return getConstant(new NameAndTypeConstant((short) -1, name, descriptor));
    }

    @NotNull
    private <C extends Constant<?>> C getConstant(@NotNull C nullConstant) {
        short i = constantToIndex.computeIfAbsent(nullConstant, k -> {
            short index = (short) (constants.size() + 1);
            constants.add(nullConstant.withIndex(index));
            return index;
        });

        return (C) constants.get(i - 1);
    }

    @NotNull
    @UnmodifiableView
    public List<Constant<?>> constants() {
        return Collections.unmodifiableList(constants);
    }

    public void compile(@NotNull DataOutput dataOutput) throws IOException {
        if (constants.size() + 1 >= Unsigned.MAX_SHORT) {
            throw new IOException("Cannot compile ConstantPool with %d constants"
                    .formatted(constants.size()));
        }

        int size = constants.size();
        dataOutput.writeShort(size + 1);
        for (Constant<?> constant : constants) {
            constant.compile(this, dataOutput);
        }
    }
}
