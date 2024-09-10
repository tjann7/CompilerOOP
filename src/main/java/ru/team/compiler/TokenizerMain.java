package ru.team.compiler;

import java.util.Scanner;

public class TokenizerMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter ; to exit, ' to parse");
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.strip().equals(";")) {
                break;
            }

            if (line.strip().equals("'")) {
                try {
                    Tokenizer tokenizer = new Tokenizer(stringBuilder.toString());
                    while (tokenizer.hasNext()) {
                        Token token = tokenizer.next();
                        System.out.println(token);
                    }
                } catch (IllegalStateException e) {
                    System.out.println("ERROR: " + e);
                }
                stringBuilder = new StringBuilder();
                continue;
            }

            stringBuilder.append(line).append("\n");
        }

    }
}