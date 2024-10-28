package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record AnalyzableClass(@NotNull String name,
                              // КЛюч должен быть Название + Аргументы -> Рекорд (MethodKey)
                              @NotNull Map<AnalyzableMethod, String> methods,
                              @NotNull Map<AnalyzableField, String> fields,
                              @NotNull Map<AnalyzableConstructor, String> constructors) {
}
