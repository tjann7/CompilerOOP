package ru.team.compiler.util;

import org.jetbrains.annotations.NotNull;

public final class Bytes {

    private Bytes() {

    }

    public static byte @NotNull [] of(byte @NotNull ... bs) {
        return bs;
    }

    public static byte @NotNull [] byteAndShort(byte b, short s) {
        byte[] result = new byte[3];
        result[0] = b;
        result[1] = (byte) ((s % 0xFF00) >> 8);
        result[2] = (byte) ((s & 0xFF) >> 0);
        return result;
    }
}
