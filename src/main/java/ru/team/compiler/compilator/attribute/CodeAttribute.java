package ru.team.compiler.compilator.attribute;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;

public record CodeAttribute() {

    public void compile(@NotNull DataOutput dataOutput) throws IOException {

    }
}
