package com.scanner.project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TokenStream {

    private boolean isEof = false;
    private char nextChar = ' ';
    private BufferedReader input;

    public TokenStream(String fileName) {
        try {
            input = new BufferedReader(new FileReader(fileName));
            nextChar = readChar();
        } catch (IOException e) {
            isEof = true;
        }
    }

    public Token nextToken() {
        Token t = new Token();
        t.setType("Other");
        t.setValue("");

        skipWhiteSpace();

        if (isEof) return t;

        // comments
        if (nextChar == '/') {
            char c = readChar();
            if (c == '/') {
                while (!isEof && nextChar != '\n')
                    nextChar = readChar();
                return nextToken();
            } else {
                t.setType("Operator");
                t.setValue("/");
                return t;
            }
        }

        // operators
        if (isOperator(nextChar)) {
            t.setType("Operator");
            t.setValue("" + nextChar);
            char first = nextChar;
            nextChar = readChar();

            if ((first == ':' && nextChar == '=') ||
                (first == '=' && nextChar == '=') ||
                (first == '!' && nextChar == '=') ||
                (first == '|' && nextChar == '|') ||
                (first == '&' && nextChar == '&') ||
                (first == '<' && nextChar == '=') ||
                (first == '>' && nextChar == '=')) {
                t.setValue(t.getValue() + nextChar);
                nextChar = readChar();
            }

            // single "=" is invalid
            if (t.getValue().equals("=")) {
                t.setType("Other");
            }

            return t;
        }

        // separators
        if (isSeparator(nextChar)) {
            t.setType("Separator");
            t.setValue("" + nextChar);
            nextChar = readChar();
            return t;
        }

        // identifiers and keywords
        if (isLetter(nextChar)) {
            while (isLetter(nextChar) || isDigit(nextChar)) {
                t.setValue(t.getValue() + nextChar);
                nextChar = readChar();
            }

            if (isKeyword(t.getValue()))
                t.setType("Keyword");
            else if (t.getValue().equals("true") || t.getValue().equals("false"))
                t.setType("Literal");
            else
                t.setType("Identifier");

            return t;
        }

        // numbers
        if (isDigit(nextChar)) {
            t.setType("Literal");
            while (isDigit(nextChar)) {
                t.setValue(t.getValue() + nextChar);
                nextChar = readChar();
            }
            return t;
        }

        nextChar = readChar();
        return t;
    }

    private char readChar() {
        try {
            int c = input.read();
            if (c == -1) {
                isEof = true;
                return 0;
            }
            return (char) c;
        } catch (IOException e) {
            isEof = true;
            return 0;
        }
    }

    private void skipWhiteSpace() {
        while (!isEof && Character.isWhitespace(nextChar))
            nextChar = readChar();
    }

    private boolean isKeyword(String s) {
        return s.equals("main") ||
               s.equals("integer") ||
               s.equals("bool") ||
               s.equals("if") ||
               s.equals("else") ||
               s.equals("while");
    }

    private boolean isSeparator(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == ',' || c == ';';
    }

    private boolean isOperator(char c) {
        return "+-*/<>=!|&:".indexOf(c) >= 0;
    }

    private boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }
}
