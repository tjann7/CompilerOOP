package ru.team.compiler.compiler;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;

import java.util.concurrent.atomic.AtomicInteger;

public record CompilationContext(@NotNull AnalyzeContext analyzeContext, @NotNull AtomicInteger currentStackSize,
                                 @NotNull AtomicInteger maxStackSize) {

    public CompilationContext(@NotNull AnalyzeContext analyzeContext) {
        this(analyzeContext, new AtomicInteger(0), new AtomicInteger(0));
    }

    public void incrementStackSize(int size) {
        int i = currentStackSize.get() + size;
        currentStackSize.set(i);
        if (i > maxStackSize.get()) {
            maxStackSize.set(i);
        }
    }

    public void decrementStackSize(int size) {
        int i = currentStackSize.get() - size;
        if (i < 0) {
            throw new IllegalArgumentException("Stack size cannot be negative");
        }

        currentStackSize.set(i);
    }

}
