package com.scanner.project;
// TokenStream.java

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TokenStream {

	private boolean isEof = false;
	private char nextChar = ' ';
	private BufferedReader input;

	public boolean isEoFile() {
		return isEof;
	}

	public TokenStream(String fileName) {
		try {
			input = new BufferedReader(new FileReader(fileName));
			nextChar = readChar();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
			isEof = true;
		}
	}

	public Token nextToken() { 
		skipWhiteSpace();

        // --- CRITICAL FIX: Skip headers ---
        while (nextChar == '[') {
            while (nextChar != ']' && !isEof) {
                nextChar = readChar();
            }
            if (!isEof) nextChar = readChar(); // consume ']'
            skipWhiteSpace();
        }
        // ----------------------------------------------

		if (isEof) {
			Token eof = new Token();
			eof.setType("EOF");
			eof.setValue("");
			return eof;
    	}
		Token t = new Token();
		t.setType("Other");
		t.setValue("");

		// Check for comments
		while (nextChar == '/') {
			nextChar = readChar();
			if (nextChar == '/') {
				while (!isEndOfLine(nextChar) && !isEof) {
					nextChar = readChar();
				}
				skipWhiteSpace();
				if (isEof) {
					Token eof = new Token();
					eof.setType("EOF");
					eof.setValue("");
					return eof;
				}
			} else {
				t.setValue("/");
				t.setType("Operator");
				return t;
			}
		}

		if (isOperator(nextChar)) {
			t.setType("Operator");
			t.setValue("" + nextChar);
			switch (nextChar) {
			case '<':
				t.setType("Operator");
                nextChar = readChar();
                if (nextChar == '=') {
                    t.setValue("<=");
                    nextChar = readChar();
                } else if (nextChar == '>') { 
                    t.setValue("<>"); // Support for <>
                    nextChar = readChar();
                } else {
                    t.setValue("<");
                }
                return t;
				
			case '>':
				t.setType("Operator");
                nextChar = readChar();
                if (nextChar == '=') {
                    t.setValue(">=");
                    nextChar = readChar();
                } else {
                    t.setValue(">");
                }
                return t;

			case '=':
				nextChar = readChar();
                if (nextChar == '=') {
                    t.setType("Operator");
                    t.setValue("==");
                    nextChar = readChar();
                    return t;
                }
                t.setType("Other");
                t.setValue("=");
                return t;

			case '!':
				t.setType("Operator");
                nextChar = readChar();
                if (nextChar == '=') {
                    t.setValue("!=");
                    nextChar = readChar();
                } else {
                    t.setValue("!");
                }
                return t;
			case ':':
                nextChar = readChar();
                if (nextChar == '=') {
                    t.setType("Operator");
                    t.setValue(":=");
                    nextChar = readChar();
                    return t;
                }
                t.setType("Other");
                t.setValue(":");
                return t;	
			case '|':
				nextChar = readChar();
				if (nextChar == '|') {
                    t.setType("Operator");
                    t.setValue("||");
                    nextChar = readChar();
                    return t;
                }
                t.setType("Other");
                t.setValue("|");
                return t;

			case '&':
				nextChar = readChar();
                if (nextChar == '&') {
                    t.setType("Operator");
                    t.setValue("&&");
                    nextChar = readChar();
                    return t;
                }
                t.setType("Other");
                t.setValue("&");
                return t;

			default: 
				t.setType("Operator");
				nextChar = readChar();
				return t;
			}
		}

		if (isSeparator(nextChar)) {
			t.setType("Separator");
			t.setValue("" + nextChar);
        	nextChar = readChar();
			return t;
		}

		if (isLetter(nextChar)) {
			t.setType("Identifier");
			while (isLetter(nextChar) || isDigit(nextChar)) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			if (isKeyword(t.getValue())) {
				t.setType("Keyword");
			} else if (t.getValue().equalsIgnoreCase("true")) {
                // CRITICAL FIX for ptest6: Handle "True" and "true"
				t.setType("Literal");
                t.setValue("true"); // Normalize to lowercase for parser
			} else if (t.getValue().equalsIgnoreCase("false")) {
                t.setType("Literal");
                t.setValue("false"); // Normalize to lowercase for parser
            }
			return t;
		}

		if (isDigit(nextChar)) { 
			t.setType("Literal");
			while (isDigit(nextChar)) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			return t; 
		}

		t.setType("Other");
		t.setValue("" + nextChar);
		nextChar = readChar();
		return t;
	}

	private char readChar() {
		if (isEof)
			return (char) 0;
		try {
			int i = input.read();
			if (i == -1) {
				isEof = true;
				return (char) 0;
			}
			return (char) i;
		} catch (IOException e) {
			isEof = true;
			return (char) 0;
		}
	}

	private boolean isKeyword(String s) {
		return s.equals("main") ||
           s.equals("integer") ||
           s.equals("bool") ||
           s.equals("if") ||
           s.equals("else") ||
           s.equals("while");
	}

	private boolean isWhiteSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfLine(char c) {
		return (c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfToken(char c) {
		return (isWhiteSpace(c) || isOperator(c) || isSeparator(c) || isEof);
	}

	private void skipWhiteSpace() {
		while (!isEof && isWhiteSpace(nextChar)) {
			nextChar = readChar();
		}
	}

	private boolean isSeparator(char c) {
		return c == '(' || c == ')' ||
				c == '{' || c == '}' ||
				c == ',' || c == ';';
	}

	private boolean isOperator(char c) {
		return c == '+' || c == '-' ||
           c == '*' || c == '/' ||
           c == '<' || c == '>' ||
           c == '=' || c == '!' ||
           c == '|' || c == '&' ||
           c == ':';
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_');
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public boolean isEndofFile() {
		return isEof;
	}
}
