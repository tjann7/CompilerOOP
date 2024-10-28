package ru.team.compiler.analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record AnalyzeContext(@NotNull Map<String, AnalyzableClass> classes,
                             @NotNull Map<String, String> variables) {
}
