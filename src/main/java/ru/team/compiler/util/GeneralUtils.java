package ru.team.compiler.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class GeneralUtils {

    private GeneralUtils() {

    }

    @NotNull
    public static File getDeclaringJarFile(@NotNull Class<?> clazz) {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        return new File(decodedPath);
    }
}
