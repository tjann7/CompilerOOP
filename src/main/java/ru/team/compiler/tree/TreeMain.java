package ru.team.compiler.tree;

import ru.team.compiler.token.Token;
import ru.team.compiler.token.Tokenizer;
import ru.team.compiler.tree.node.NodeToStringHelper;
import ru.team.compiler.tree.node.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TreeMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ' to exit, | to parse");
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String code = scanner.nextLine();
            if (code.strip().equals("'")) {
                break;
            }

            if (code.strip().equals("|")) {
                try {
                    Tokenizer tokenizer = new Tokenizer(stringBuilder.toString());
                    List<Token> tokens = new ArrayList<>();
                    while (tokenizer.hasNext()) {
                        tokens.add(tokenizer.next());
                    }

                    TreeNode node = TreeNode.PARSER.parse(tokens);
                    System.out.println(NodeToStringHelper.toString(node, true));
                } catch (Exception e) {
                    System.out.println("ERROR: " + e);
                }
                stringBuilder = new StringBuilder();

                continue;
            }

            stringBuilder.append(code).append("\n");
        }
    }
}
