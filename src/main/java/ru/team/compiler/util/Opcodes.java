package ru.team.compiler.util;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compiler.constant.ConstantPool;

public interface Opcodes {

    int ICONST_M1 = 2;
    int ICONST_0 = 3;
    int ICONST_1 = 4;
    int ICONST_2 = 5;
    int ICONST_3 = 6;
    int ICONST_4 = 7;
    int ICONST_5 = 8;

    int FCONST_0 = 11;
    int FCONST_1 = 12;
    int FCONST_2 = 13;

    int BIPUSH = 16;
    int SIPUSH = 17;

    int LDC = 18;
    int LDC_W = 19;

    int ALOAD = 25;
    int ALOAD_0 = 42;
    int ALOAD_1 = 43;
    int ALOAD_2 = 44;
    int ALOAD_3 = 45;

    int ASTORE = 58;
    int ASTORE_0 = 75;
    int ASTORE_1 = 76;
    int ASTORE_2 = 77;
    int ASTORE_3 = 78;

    int POP = 87;

    int DUP = 89;

    int IFEQ = 153;

    int GOTO = 167;

    int ARETURN = 176;
    int RETURN = 177;

    int GETFIELD = 180;
    int PUTFIELD = 181;

    int INVOKEVIRTUAL = 182;
    int INVOKESPECIAL = 183;

    int NEW = 187;

    static byte @NotNull [] iconst(@NotNull ConstantPool constantPool, int value) {
        return switch (value) {
            case -1 -> Bytes.of((byte) ICONST_M1);
            case 0 -> Bytes.of((byte) ICONST_0);
            case 1 -> Bytes.of((byte) ICONST_1);
            case 2 -> Bytes.of((byte) ICONST_2);
            case 3 -> Bytes.of((byte) ICONST_3);
            case 4 -> Bytes.of((byte) ICONST_4);
            case 5 -> Bytes.of((byte) ICONST_5);
            default -> {
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
                    yield Bytes.of((byte) BIPUSH, (byte) value);
                } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
                    yield Bytes.byteAndShort((byte) SIPUSH, (short) value);
                } else {
                    short index = constantPool.getInt(value).index();

                    if (Byte.MIN_VALUE <= index && index <= Byte.MAX_VALUE) {
                        yield Bytes.of((byte) LDC, (byte) index);
                    } else {
                        yield Bytes.byteAndShort((byte) LDC_W, index);
                    }
                }
            }
        };
    }

    static byte @NotNull [] fconst(@NotNull ConstantPool constantPool, float value) {
        if (value == 0.0f) {
            return Bytes.of((byte) FCONST_0);
        } else if (value == 1.0f) {
            return Bytes.of((byte) FCONST_1);
        } else if (value == 2.0f) {
            return Bytes.of((byte) FCONST_2);
        }

        short index = constantPool.getFloat(value).index();

        if (Byte.MIN_VALUE <= index && index <= Byte.MAX_VALUE) {
            return Bytes.of((byte) LDC, (byte) index);
        } else {
            return Bytes.byteAndShort((byte) LDC_W, index);
        }
    }

    static byte @NotNull [] aload(@NotNull ConstantPool constantPool, int value) {
        return switch (value) {
            case 0 -> Bytes.of((byte) ALOAD_0);
            case 1 -> Bytes.of((byte) ALOAD_1);
            case 2 -> Bytes.of((byte) ALOAD_2);
            case 3 -> Bytes.of((byte) ALOAD_3);
            default -> Bytes.byteAndShort((byte) ALOAD, constantPool.getInt(value).index());
        };
    }

    static byte @NotNull [] astore(@NotNull ConstantPool constantPool, int value) {
        return switch (value) {
            case 0 -> Bytes.of((byte) ASTORE_0);
            case 1 -> Bytes.of((byte) ASTORE_1);
            case 2 -> Bytes.of((byte) ASTORE_2);
            case 3 -> Bytes.of((byte) ASTORE_3);
            default -> Bytes.byteAndShort((byte) ASTORE, constantPool.getInt(value).index());
        };
    }

}
