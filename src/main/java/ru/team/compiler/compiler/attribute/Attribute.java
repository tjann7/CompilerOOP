package ru.team.compiler.compiler.attribute;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compiler.CompilationContext;
import ru.team.compiler.compiler.constant.ConstantPool;
import ru.team.compiler.compiler.constant.Utf8Constant;

import java.io.DataOutput;
import java.io.IOException;

public abstract sealed class Attribute permits CodeAttribute {

    protected final Utf8Constant attributeName;

    public Attribute(@NotNull Utf8Constant utfConstant) {
        this.attributeName = utfConstant;
    }

    public abstract void compile(@NotNull CompilationContext context, @NotNull ConstantPool constantPool,
                                 @NotNull DataOutput dataOutput) throws IOException;

}
