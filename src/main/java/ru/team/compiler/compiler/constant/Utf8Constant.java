package ru.team.compiler.compiler.constant;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.util.Unsigned;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Utf8Constant extends Constant<String> {

    public Utf8Constant(short index, @NotNull String value) {
        super(index, value);
    }

    @Override
    protected Utf8Constant withIndex(short index) {
        return new Utf8Constant(index, value);
    }

    @Override
    public void compile(@NotNull ConstantPool constantPool, @NotNull DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(1);

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > Unsigned.MAX_SHORT) {
            throw new IOException("Cannot compile Utf8Constant with %d-length string: %s"
                    .formatted(value.length(), value));
        }

        dataOutput.writeShort(bytes.length);
        dataOutput.write(bytes);
    }
}
