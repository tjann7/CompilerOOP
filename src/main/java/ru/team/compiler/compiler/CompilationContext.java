package ru.team.compiler.compiler;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.analyzer.AnalyzeContext;

public record CompilationContext(@NotNull AnalyzeContext analyzeContext) {

}
