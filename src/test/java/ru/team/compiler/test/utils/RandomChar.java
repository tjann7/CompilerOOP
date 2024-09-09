package ru.team.compiler.test.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomChar {

    private final Random random;

    public RandomChar(@NotNull Random random) {
        this.random = random;
    }

    public char nextLowerCaseLetter() {
        return (char) random.nextInt('a', 'z' + 1);
    }

    public char nextUpperCaseLetter() {
        return (char) random.nextInt('A', 'Z' + 1);
    }

    public char nextDigit() {
        return (char) random.nextInt('0', '9' + 1);
    }

    public char next() {
        if (random.nextBoolean()) {
            return nextLowerCaseLetter();
        } else if (random.nextBoolean()) {
            return nextUpperCaseLetter();
        } else {
            return nextDigit();
        }
    }
}
