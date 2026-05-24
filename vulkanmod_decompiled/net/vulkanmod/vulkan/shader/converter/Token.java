/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.shader.converter;

public class Token {
    public final TokenType type;
    public String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return "Token{type=" + String.valueOf((Object)this.type) + ", value='" + this.value + "'}";
    }

    public static enum TokenType {
        PREPROCESSOR,
        KEYWORD,
        IDENTIFIER,
        LITERAL,
        OPERATOR,
        PUNCTUATION,
        STRING,
        SPACING,
        COMMENT,
        LEFT_BRACE,
        RIGHT_BRACE,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        COLON,
        SEMICOLON,
        DOT,
        COMMA,
        TYPE,
        LAYOUT,
        EOF;

    }
}

