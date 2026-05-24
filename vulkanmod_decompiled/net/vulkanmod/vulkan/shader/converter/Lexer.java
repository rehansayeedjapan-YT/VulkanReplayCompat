/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.converter;

import java.util.ArrayList;
import java.util.List;
import net.vulkanmod.vulkan.shader.converter.Token;

public class Lexer {
    private final String input;
    private int currentPosition;
    private char currentChar;
    private State state;

    public Lexer(String input) {
        this.input = input;
        this.currentPosition = 0;
        this.currentChar = !input.isEmpty() ? input.charAt(0) : (char)'\u0000';
    }

    private void advance() {
        this.advance(1);
    }

    private void advance(int i) {
        for (int j = 0; j < i; ++j) {
            ++this.currentPosition;
            if (this.currentPosition >= this.input.length()) {
                this.currentChar = '\u0000';
                break;
            }
            this.currentChar = this.input.charAt(this.currentPosition);
        }
    }

    private char peek() {
        int peekPosition = this.currentPosition + 1;
        if (peekPosition < this.input.length()) {
            return this.input.charAt(peekPosition);
        }
        return '\u0000';
    }

    public List<Token> tokenize() {
        ArrayList<Token> tokens = new ArrayList<Token>();
        while (this.currentPosition < this.input.length()) {
            char currentChar = this.input.charAt(this.currentPosition);
            Token token = this.nextToken();
            if (token != null) {
                tokens.add(token);
                continue;
            }
            throw new RuntimeException("Unknown character: " + currentChar);
        }
        tokens.add(new Token(Token.TokenType.EOF, null));
        return tokens;
    }

    public Token nextToken() {
        Token token;
        if (!this.checkEOF()) {
            return new Token(Token.TokenType.EOF, null);
        }
        if (this.currentChar == '/' && this.peek() == '/') {
            this.advance(2);
            return this.comment();
        }
        switch (this.currentChar) {
            case '=': {
                if (this.peek() != '=') break;
                this.advance(2);
                return new Token(Token.TokenType.OPERATOR, "==");
            }
            case '!': {
                if (this.peek() != '=') break;
                this.advance(2);
                return new Token(Token.TokenType.OPERATOR, "!=");
            }
            case '<': {
                switch (this.peek()) {
                    case '=': {
                        this.advance(2);
                        return new Token(Token.TokenType.OPERATOR, "<=");
                    }
                    case '<': {
                        this.advance(2);
                        return new Token(Token.TokenType.OPERATOR, "<<");
                    }
                }
                break;
            }
            case '>': {
                switch (this.peek()) {
                    case '=': {
                        this.advance(2);
                        return new Token(Token.TokenType.OPERATOR, ">=");
                    }
                    case '>': {
                        this.advance(2);
                        return new Token(Token.TokenType.OPERATOR, ">>");
                    }
                }
            }
        }
        switch (this.currentChar) {
            case '{': {
                Token token2 = new Token(Token.TokenType.LEFT_BRACE, "{");
                break;
            }
            case '}': {
                Token token2 = new Token(Token.TokenType.RIGHT_BRACE, "}");
                break;
            }
            case '(': {
                Token token2 = new Token(Token.TokenType.LEFT_PARENTHESIS, "(");
                break;
            }
            case ')': {
                Token token2 = new Token(Token.TokenType.RIGHT_PARENTHESIS, ")");
                break;
            }
            case ':': {
                Token token2 = new Token(Token.TokenType.COLON, ":");
                break;
            }
            case ';': {
                Token token2 = new Token(Token.TokenType.SEMICOLON, ";");
                break;
            }
            case '.': {
                Token token2 = new Token(Token.TokenType.DOT, ".");
                break;
            }
            case ',': {
                Token token2 = new Token(Token.TokenType.COMMA, ",");
                break;
            }
            case '=': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "=");
                break;
            }
            case '+': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "+");
                break;
            }
            case '-': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "-");
                break;
            }
            case '*': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "*");
                break;
            }
            case '/': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "/");
                break;
            }
            case '%': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "%");
                break;
            }
            case '<': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "<");
                break;
            }
            case '>': {
                Token token2 = new Token(Token.TokenType.OPERATOR, ">");
                break;
            }
            case '!': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "!");
                break;
            }
            case '&': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "&");
                break;
            }
            case '|': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "|");
                break;
            }
            case '^': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "^");
                break;
            }
            case '?': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "?");
                break;
            }
            case '[': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "[");
                break;
            }
            case ']': {
                Token token2 = new Token(Token.TokenType.OPERATOR, "]");
                break;
            }
            case '#': {
                Token token2 = new Token(Token.TokenType.PREPROCESSOR, "#");
                break;
            }
            case '\"': {
                Token token2 = this.string();
                break;
            }
            default: {
                Token token2 = token = null;
            }
        }
        if (token == null) {
            if (Character.isLetter(this.currentChar)) {
                return this.identifier();
            }
            if (Character.isDigit(this.currentChar)) {
                return this.literal();
            }
            if (Character.isWhitespace(this.currentChar)) {
                return this.spacing();
            }
        }
        if (token == null) {
            throw new IllegalStateException("Unrecognized char: " + this.currentChar);
        }
        this.advance();
        return token;
    }

    private Token comment() {
        StringBuilder sb = new StringBuilder();
        sb.append("//");
        while (this.checkEOF() && this.currentChar != '\n') {
            sb.append(this.currentChar);
            this.advance();
        }
        sb.append(this.currentChar);
        this.advance();
        String value = sb.toString();
        return new Token(Token.TokenType.COMMENT, value);
    }

    private Token identifier() {
        StringBuilder sb = new StringBuilder();
        while (this.checkEOF() && Character.isJavaIdentifierPart(this.currentChar)) {
            sb.append(this.currentChar);
            this.advance();
        }
        String value = sb.toString();
        return new Token(Token.TokenType.IDENTIFIER, value);
    }

    private Token literal() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(this.currentChar)) {
            sb.append(this.currentChar);
            this.advance();
        }
        if (this.currentChar == '.') {
            sb.append(this.currentChar);
            this.advance();
        }
        while (Character.isDigit(this.currentChar)) {
            sb.append(this.currentChar);
            this.advance();
        }
        String value = sb.toString();
        return new Token(Token.TokenType.LITERAL, value);
    }

    private Token string() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.currentChar);
        this.advance();
        while (this.checkEOF() && this.currentChar != '\"') {
            sb.append(this.currentChar);
            this.advance();
        }
        sb.append(this.currentChar);
        this.advance();
        String value = sb.toString();
        return new Token(Token.TokenType.STRING, value);
    }

    private Token spacing() {
        StringBuilder sb = new StringBuilder();
        while (this.currentChar != '\u0000' && Character.isWhitespace(this.currentChar)) {
            sb.append(this.currentChar);
            this.advance();
        }
        String value = sb.toString();
        return new Token(Token.TokenType.SPACING, value);
    }

    private boolean checkEOF() {
        return this.currentChar != '\u0000';
    }

    static enum State {
        UNIFORM_BLOCK,
        CODE,
        DEFAULT;

    }
}

