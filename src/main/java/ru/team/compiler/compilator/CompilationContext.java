package ru.team.compiler.compilator;

import org.jetbrains.annotations.NotNull;
import ru.team.compiler.compilator.constant.ConstantPool;

public record CompilationContext(@NotNull ConstantPool constantPool) {
}
