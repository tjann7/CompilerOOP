import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

enum TokenType {
    IntegerLiteral,
    RealLiteral,
    BooleanLiteral,
    Identifier,
    Keyword,
    Brackets,
    Unidentified,
    Colon,
    Semicolon,
    Equals,


}



class Token {
    Token(String lexeme, TokenType type) {
        this.lexeme = lexeme;
        this.type = type;
    }
    public final String lexeme;
    public final TokenType type;
}

class Tokenizer {

    private final List<String> operators;
    private final List<String> literals;
    private final List<String> keywords;
    private final List<Character> brackets;
    private final List<Character> special_symbols;

    private final int DIGIT_LOW = 48;
    private final int DIGIT_HIGH = 57;
    private final int UPPER_LETTER_LOW = 65;
    private final int UPPER_LETTER_HIGH = 90;
    private final int LOWER_LETTER_LOW = 97;
    private final int LOWER_LETTER_HIGH = 122;
    private final int UNDERSCORE = 95;

    private final String IF = "if";
    private final String THEN = "then";
    private final String ELSE = "else";
    private final String WHILE = "while";
    private final String THIS = "this";
    private final String IS = "is";
    private final String END = "end";
    private final String RETURN = "return";
    private final String METHOD = "method";
    private final String CLASS = "class";
    private final String EXTENDS = "extends";
    private final String VAR = "var";


    Tokenizer() {
        this.operators = new ArrayList<>(Arrays.asList(":=", "!", ""));
        this.literals = new ArrayList<>();
        this.keywords = new ArrayList<>((
                Arrays.asList(IF, THEN, ELSE, WHILE,
                        THIS, IS, END, RETURN,
                        METHOD, CLASS, EXTENDS, VAR)));
        this.brackets = new ArrayList<>((Arrays.asList('\"', '(', ')', '[', ']', '{', '}')));
        this.special_symbols = new ArrayList<>(Arrays.asList(
                '!', '%', '*', '+', ',', '-', '.', '/', ':', ';',
                '<', '=', '>', '?', '\\', '^', '_', '|', ' ', '\"',
                '\'', '(', ')', '[', ']', '{', '}'));
    }

    TokenType check_identifier(String word) {
        // TODO: Optimize by checking length first
        if (keywords.contains(word))
            return TokenType.Keyword;
        else
            return null;
    }

    List<Token> tokenize(String code) {
        List<Token> ret = new ArrayList<>();
        int pos = 0, flag;
        String word;
        char val;
        TokenType type;
        while (pos < code.length()) {
            flag = 0;
            word = "";
            type = null;
            try {
                val = code.charAt(pos);
                if (((UPPER_LETTER_LOW <= val) && (val <= UPPER_LETTER_HIGH))
                        || ((LOWER_LETTER_LOW <= val) && (val <= LOWER_LETTER_HIGH))) {
                    /* an identifier case */
                    while ((pos < code.length())
                            && (((DIGIT_LOW <= val) && (val <= DIGIT_HIGH))
                            || ((UPPER_LETTER_LOW <= val) && (val <= UPPER_LETTER_HIGH))
                            || ((LOWER_LETTER_LOW <= val) && (val <= LOWER_LETTER_HIGH))
                            || (val == UNDERSCORE))) {
                        word += val;
                        type = check_identifier(word);
                        if (type == null) {
                            ++pos;
                            val = code.charAt(pos);
                        } else {
                            ret.add(new Token(word, type));
                            ++pos;
                            break;
                        }
                    }
                    if (val == '.') {
                        ret.add(new Token(word, TokenType.Identifier));
                        ret.add(new Token(""))
                    } else if (type == null)
                        ret.add(new Token(word, TokenType.Identifier));

                } else if ((48 <= val) && (val <= 57)) {
                    /* A digit case */
                    while (pos < code.length()
                            && (((DIGIT_LOW <= val)
                            && (val <= DIGIT_HIGH))
                            || (val == '.'))) {
                        if (val == '.') {
                            if (flag == 0) ++flag;
                            else {
                                /* Unidentified token found: double dotting in number */
                                while ((pos < code.length() && (val != ' '))) {
                                    word += val;
                                    ++pos;
                                    val = code.charAt(pos);
                                }
                                ret.add(new Token(word, TokenType.Unidentified));
                                break;
                            }

                        }
                        word += val;
                        ++pos;
                        if (pos == code.length()) break;
                        val = code.charAt(pos);
                    }
                    if ((pos != code.length()) && (((UPPER_LETTER_LOW <= val)
                            && (val <= UPPER_LETTER_HIGH))
                            || ((LOWER_LETTER_LOW <= val)
                            && (val <= LOWER_LETTER_HIGH))
                            || val == UNDERSCORE)) {
                        /* Unidentified token found: Identifier begins with digits */
                        while ((pos < code.length() && (val != 32))) { /* 32 - ' ' */
                            word += val;
                            ++pos;
                            if (pos == code.length()) break;
                            val = code.charAt(pos);
                        }
                        ret.add(new Token(word, TokenType.Unidentified));
                    } else if (flag == 0) {
                        ret.add(new Token(word, TokenType.IntegerLiteral));
                    }
                    else {
                        ret.add(new Token(word, TokenType.RealLiteral));
                    }
                    // TODO Check for remaining errors
                } else if (brackets.contains(val)) {
                    /* Brackets case */
                    word += val;
                    ret.add(new Token(word, TokenType.Brackets));
                    ++pos;
                } else if (val == ':') {
                    word += val;
                    ++pos;
                    if (pos == code.length()) break;
                    val = code.charAt(pos);
                    if (val == '=') {
                        word += val;
                        ret.add(new Token(word, TokenType.Equals));
                        ++pos;
                    } else {
                        ret.add(new Token(word, TokenType.Colon));
                    }
                } else if (val == ';') {
                    word += val;
                    ++pos;
                    ret.add(new Token(word, TokenType.Semicolon));
                } else if (val == '\'') {
                    word += val;

                } else if (val == '\"') {

                } else {
                    ++pos;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return ret;
    }
}

public class Main {
    public static void main(String[] args) {
        Tokenizer tokenizer = new Tokenizer();
        String code = "";
        try {
            Scanner sc = new Scanner(new File("/home/tjann/IdeaProjects/CompilerOOP/src/testing"));
            while (sc.hasNextLine()) {
                code += sc.nextLine() + "\n";
            }
            for (Token i: tokenizer.tokenize(code)) {
                System.out.println(i.lexeme + " " + i.type);
            }
        } catch (FileNotFoundException f) {
            System.out.println(f.getMessage());
        }
    }
}