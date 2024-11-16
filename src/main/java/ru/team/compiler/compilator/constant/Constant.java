package ru.team.compiler.compilator.constant;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public abstract sealed class Constant<T> permits ClassConstant, FieldRefConstant, MethodRefConstant,
                                                 IntegerConstant, FloatConstant,
                                                 NameAndTypeConstant, Utf8Constant {

    protected final short index;
    protected final T value;

    public Constant(short index, @NotNull T value) {
        this.index = index;
        this.value = value;
    }

    public final short index() {
        return index;
    }

    @NotNull
    public final T value() {
        return value;
    }

    protected abstract Constant<T> withIndex(short index);

    public abstract void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Constant<?> constant = (Constant<?>) object;
        return index == constant.index && Objects.equals(value, constant.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "index=" + index +
                ", value=" + value +
                '}';
    }
}
