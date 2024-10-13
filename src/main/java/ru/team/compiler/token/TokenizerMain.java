package ru.team.compiler.token;

import java.util.Scanner;

public class TokenizerMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ' to exit, ; to parse");
        StringBuilder stringBuilder = new StringBuilder();
        int line = 0;
        int holdLines = 0;
        while (true) {
            String code = scanner.nextLine();
            if (code.strip().equals("'")) {
                break;
            }

            if (code.strip().equals(";")) {
                try {
                    Tokenizer tokenizer = new Tokenizer(stringBuilder.toString(), line);
                    while (tokenizer.hasNext()) {
                        System.out.println("DUDE");
                        Token token = tokenizer.next();
                        System.out.println(token);
                    }
                } catch (IllegalStateException e) {
                    System.out.println("ERROR: " + e);
                }
                stringBuilder = new StringBuilder();
                line += holdLines + 1;
                holdLines = 0;
                continue;
            }

            ++holdLines;
        }
    }
}
