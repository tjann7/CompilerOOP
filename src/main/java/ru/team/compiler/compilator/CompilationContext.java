package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;

public record CompilationContext(@NotNull AnalyzeContext analyzeContext) {

}
