package ru.team.compiler.compiler.main;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CompilerMain {

    private CompilerMain() {

    }

    public static void main(String[] args) {
        List<String> arguments = new ArrayList<>();
        Set<String> options = new HashSet<>();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                options.add(arg);
            } else {
                arguments.add(arg);
            }
        }

        if (arguments.size() != 1) {
            printUsage();
            return;
        }

        Path path = Path.of(arguments.get(0));

        ClassCompilation.compile(path, options);
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar olang [-bundle] <file>");
    }

}
