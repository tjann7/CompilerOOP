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
            stringBuilder.append(code).append(" \n");
        }
        try {
            Tokenizer tokenizer = new Tokenizer(stringBuilder.toString());
            while (tokenizer.hasNext()) {
                Token token = tokenizer.next();
                System.out.println(token);
            }
        } catch (IllegalStateException e) {
            System.out.println("ERROR: " + e);
        }
    }
}
