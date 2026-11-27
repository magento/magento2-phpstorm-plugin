/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Package-private JavaScript lexer producing lightweight tokens with character offsets.
 */
final class JsLexer {
    enum TokenType {
        IDENTIFIER,
        STRING,
        NUMBER,
        REGEX,
        LBRACE,
        RBRACE,
        LPAREN,
        RPAREN,
        LBRACKET,
        RBRACKET,
        COLON,
        COMMA,
        SEMICOLON,
        EQUALS,
        PUNCTUATION
    }

    record Token(@NotNull TokenType type, @NotNull String text, int startOffset, int endOffset) { }

    private final CharSequence source;
    private final int length;
    private final List<Token> tokens = new ArrayList<>();

    private JsLexer(final @NotNull CharSequence source) {
        this.source = source;
        this.length = source.length();
    }

    static @NotNull List<Token> tokenize(final @NotNull CharSequence source) {
        return new JsLexer(source).scanAll();
    }

    private @NotNull List<Token> scanAll() {
        int i = 0;
        while (i < length) {
            final char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '/' && charAtIs(i + 1, '/')) {
                i = skipLineComment(i + 2);
            } else if (c == '/' && charAtIs(i + 1, '*')) {
                i = skipBlockComment(i + 2);
            } else if (c == '\'' || c == '"' || c == '`') {
                i = readString(i, c);
            } else if (c == '/' && canPrecedeRegex()) {
                i = readRegex(i);
            } else if (c == '=' && charAtIs(i + 1, '>')) {
                tokens.add(new Token(TokenType.PUNCTUATION, "=>", i, i + 2));
                i += 2;
            } else if (c == '=' && !charAtIs(i + 1, '=')) {
                tokens.add(new Token(TokenType.EQUALS, "=", i, i + 1));
                i++;
            } else if (Character.isJavaIdentifierStart(c)) {
                i = readIdentifier(i);
            } else if (Character.isDigit(c)) {
                i = readNumber(i);
            } else {
                final TokenType singleCharType = singleCharTokenType(c);
                final TokenType type = singleCharType != null ? singleCharType : TokenType.PUNCTUATION;
                tokens.add(new Token(type, String.valueOf(c), i, i + 1));
                i++;
            }
        }
        return tokens;
    }

    private boolean charAtIs(final int index, final char expected) {
        return index < length && source.charAt(index) == expected;
    }

    private int skipLineComment(int i) {
        while (i < length && source.charAt(i) != '\n' && source.charAt(i) != '\r') {
            i++;
        }
        return i;
    }

    private int skipBlockComment(int i) {
        while (i + 1 < length && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
            i++;
        }
        return Math.min(length, i + 2);
    }

    private int readString(final int start, final char quote) {
        int i = start + 1;
        while (i < length) {
            final char c = source.charAt(i);
            i++;
            if (c == '\\') {
                if (i < length) {
                    i++;
                }
            } else if (c == quote) {
                break;
            }
        }
        tokens.add(new Token(TokenType.STRING, source.subSequence(start, i).toString(), start, i));
        return i;
    }

    private int readRegex(final int start) {
        int i = start + 1;
        boolean inCharClass = false;
        while (i < length) {
            final char c = source.charAt(i);
            if (c == '\n' || c == '\r') {
                break;
            }
            i++;
            if (c == '\\') {
                if (i < length) {
                    i++;
                }
            } else if (c == '[') {
                inCharClass = true;
            } else if (c == ']') {
                inCharClass = false;
            } else if (c == '/' && !inCharClass) {
                while (i < length && Character.isLetter(source.charAt(i))) {
                    i++;
                }
                break;
            }
        }
        tokens.add(new Token(TokenType.REGEX, source.subSequence(start, i).toString(), start, i));
        return i;
    }

    private int readIdentifier(final int start) {
        int i = start + 1;
        while (i < length && Character.isJavaIdentifierPart(source.charAt(i))) {
            i++;
        }
        tokens.add(new Token(TokenType.IDENTIFIER, source.subSequence(start, i).toString(), start, i));
        return i;
    }

    private int readNumber(final int start) {
        int i = start + 1;
        while (i < length && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '.')) {
            i++;
        }
        tokens.add(new Token(TokenType.NUMBER, source.subSequence(start, i).toString(), start, i));
        return i;
    }

    private boolean canPrecedeRegex() {
        if (tokens.isEmpty()) {
            return true;
        }
        final int lastIndex = tokens.size() - 1;
        final Token prev = tokens.get(lastIndex);
        return switch (prev.type()) {
            case LPAREN, LBRACKET, LBRACE, COLON, COMMA, SEMICOLON, EQUALS, PUNCTUATION -> true;
            case RPAREN -> isControlFlowHeaderEnd(lastIndex);
            case IDENTIFIER -> isRegexPrefixKeyword(prev.text());
            default -> false;
        };
    }

    private boolean isControlFlowHeaderEnd(final int closeParenIndex) {
        int depth = 0;
        for (int i = closeParenIndex; i >= 0; i--) {
            final TokenType type = tokens.get(i).type();
            if (type == TokenType.RPAREN) {
                depth++;
            } else if (type == TokenType.LPAREN) {
                depth--;
                if (depth == 0) {
                    if (i == 0) {
                        return false;
                    }
                    if (i >= 2 && tokens.get(i - 2).type() == TokenType.PUNCTUATION
                            && ".".equals(tokens.get(i - 2).text())) {
                        return false;
                    }
                    final Token beforeParen = tokens.get(i - 1);
                    return beforeParen.type() == TokenType.IDENTIFIER
                            && isControlFlowParenKeyword(beforeParen.text());
                }
            }
        }
        return false;
    }

    private static boolean isControlFlowParenKeyword(final @NotNull String keyword) {
        return "if".equals(keyword)
                || "while".equals(keyword)
                || "for".equals(keyword)
                || "with".equals(keyword);
    }

    private static boolean isRegexPrefixKeyword(final @NotNull String keyword) {
        return "return".equals(keyword)
                || "typeof".equals(keyword)
                || "case".equals(keyword)
                || "else".equals(keyword)
                || "do".equals(keyword)
                || "throw".equals(keyword)
                || "in".equals(keyword)
                || "instanceof".equals(keyword)
                || "of".equals(keyword)
                || "new".equals(keyword)
                || "delete".equals(keyword)
                || "void".equals(keyword)
                || "await".equals(keyword)
                || "yield".equals(keyword);
    }

    private static @Nullable TokenType singleCharTokenType(final char c) {
        return switch (c) {
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '[' -> TokenType.LBRACKET;
            case ']' -> TokenType.RBRACKET;
            case ':' -> TokenType.COLON;
            case ',' -> TokenType.COMMA;
            case ';' -> TokenType.SEMICOLON;
            default -> null;
        };
    }
}
