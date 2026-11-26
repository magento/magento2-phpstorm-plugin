/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.magento.idea.magento2plugin.util.magento.js.JsLexer.Token;
import com.magento.idea.magento2plugin.util.magento.js.JsLexer.TokenType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stateful recursive parser converting {@link JsLexer.Token} sequences into a {@link JsAst}.
 */
final class JsObjectParser {
    private final CharSequence source;
    private final List<Token> tokens;
    private final List<JsObject> allObjects = new ArrayList<>();
    private final List<JsProperty> allProperties = new ArrayList<>();
    private final Map<String, List<JsObject>> variableObjects = new LinkedHashMap<>();

    private JsObjectParser(final @NotNull CharSequence source, final @NotNull List<Token> tokens) {
        this.source = source;
        this.tokens = tokens;
    }

    static @NotNull JsAst parse(final @NotNull CharSequence source) {
        final List<Token> tokens = JsLexer.tokenize(source);
        return new JsObjectParser(source, tokens).parseAll();
    }

    private @NotNull JsAst parseAll() {
        parseBlock(0, tokens.size(), null);
        return new JsAst(allObjects, allProperties, variableObjects);
    }

    private void parseBlock(
            final int startIndex,
            final int endIndex,
            final @Nullable JsProperty enclosingProperty
    ) {
        int i = startIndex;
        while (i < endIndex) {
            final Token token = tokens.get(i);
            if (token.type() == TokenType.LBRACE) {
                i = parseBraceGroup(i, endIndex, enclosingProperty, null);
            } else if (isVarDeclarationKeyword(token)) {
                i = parseVarDeclarations(i + 1, endIndex, enclosingProperty);
            } else {
                i++;
            }
        }
    }

    private int parseVarDeclarations(
            final int startIndex,
            final int endIndex,
            final @Nullable JsProperty enclosingProperty
    ) {
        int i = startIndex;
        while (i < endIndex) {
            final Token token = tokens.get(i);
            if (token.type() == TokenType.SEMICOLON) {
                return i + 1;
            }
            if (token.type() == TokenType.IDENTIFIER
                    && i + 2 < endIndex
                    && tokens.get(i + 1).type() == TokenType.EQUALS
                    && tokens.get(i + 2).type() == TokenType.LBRACE) {
                final String varName = token.text();
                final int closeIndex = findMatchingClose(i + 2, endIndex, TokenType.LBRACE, TokenType.RBRACE);
                final JsObject obj = parseObjectLiteral(i + 2, closeIndex, enclosingProperty, varName);
                variableObjects.computeIfAbsent(varName, ignored -> new ArrayList<>()).add(obj);
                i = closeIndex + 1;
                if (i < endIndex && tokens.get(i).type() == TokenType.COMMA) {
                    i++;
                }
            } else if (token.type() == TokenType.LBRACE) {
                i = parseBraceGroup(i, endIndex, enclosingProperty, null);
            } else {
                i++;
            }
        }
        return i;
    }

    private int parseBraceGroup(
            final int openBraceIndex,
            final int endIndex,
            final @Nullable JsProperty enclosingProperty,
            final @Nullable String assignedVariableName
    ) {
        final int closeIndex = findMatchingClose(openBraceIndex, endIndex, TokenType.LBRACE, TokenType.RBRACE);
        if (isObjectLiteralAt(openBraceIndex)) {
            parseObjectLiteral(openBraceIndex, closeIndex, enclosingProperty, assignedVariableName);
        } else {
            parseBlock(openBraceIndex + 1, closeIndex, enclosingProperty);
        }
        return closeIndex + 1;
    }

    private @NotNull JsObject parseObjectLiteral(
            final int openBraceIndex,
            final int closeBraceIndex,
            final @Nullable JsProperty parentProperty,
            final @Nullable String assignedVariableName
    ) {
        final Token openToken = tokens.get(openBraceIndex);
        final Token closeToken = closeBraceIndex < tokens.size()
                ? tokens.get(closeBraceIndex)
                : tokens.get(tokens.size() - 1);
        final JsObject jsObject = new JsObject(
                openToken.startOffset(),
                closeToken.endOffset(),
                parentProperty,
                assignedVariableName
        );
        allObjects.add(jsObject);

        int i = openBraceIndex + 1;
        while (i < closeBraceIndex) {
            final Token current = tokens.get(i);
            if (current.type() == TokenType.COMMA || current.type() == TokenType.SEMICOLON) {
                i++;
                continue;
            }
            if (current.type() == TokenType.PUNCTUATION && ".".equals(current.text())) {
                i = skipEntry(i, closeBraceIndex, parentProperty);
                continue;
            }
            final int nextAfterMethod = tryParseMethodShorthand(i, closeBraceIndex, jsObject);
            if (nextAfterMethod > i) {
                i = nextAfterMethod;
                continue;
            }
            final int nextAfterProperty = tryParseKeyValueProperty(i, closeBraceIndex, jsObject);
            if (nextAfterProperty > i) {
                i = nextAfterProperty;
                continue;
            }
            if (isPropertyKeyToken(current) && i + 1 < closeBraceIndex
                    && tokens.get(i + 1).type() == TokenType.COLON) {
                break;
            }
            i = skipEntry(i, closeBraceIndex, parentProperty);
        }

        return jsObject;
    }

    private int tryParseMethodShorthand(
            final int keyIndex,
            final int closeBraceIndex,
            final @NotNull JsObject owner
    ) {
        final Token keyToken = tokens.get(keyIndex);
        if ((keyToken.type() != TokenType.IDENTIFIER && keyToken.type() != TokenType.STRING)
                || keyIndex + 1 >= closeBraceIndex
                || tokens.get(keyIndex + 1).type() != TokenType.LPAREN) {
            return keyIndex;
        }
        final int closeParen = findMatchingClose(keyIndex + 1, closeBraceIndex, TokenType.LPAREN, TokenType.RPAREN);
        if (closeParen + 1 >= closeBraceIndex || tokens.get(closeParen + 1).type() != TokenType.LBRACE) {
            return keyIndex;
        }
        final int closeBody = findMatchingClose(closeParen + 1, closeBraceIndex, TokenType.LBRACE, TokenType.RBRACE);
        final Token bodyEnd = closeBody < tokens.size()
                ? tokens.get(closeBody)
                : tokens.get(tokens.size() - 1);
        final int valueStartOffset = tokens.get(keyIndex + 1).startOffset();
        final String valueText = "function" + source.subSequence(valueStartOffset, bodyEnd.endOffset());

        final JsProperty prop = new JsProperty(
                unquoteKey(keyToken),
                keyToken.startOffset(),
                keyToken.endOffset(),
                valueStartOffset,
                bodyEnd.endOffset(),
                valueText,
                null,
                owner
        );
        owner.addProperty(prop);
        allProperties.add(prop);
        parseBlock(closeParen + 2, closeBody, prop);
        return closeBody + 1;
    }

    private int tryParseKeyValueProperty(
            final int keyIndex,
            final int closeBraceIndex,
            final @NotNull JsObject owner
    ) {
        final Token keyToken = tokens.get(keyIndex);
        final int valStartIdx = keyIndex + 2;
        if (!isPropertyKeyToken(keyToken)
                || keyIndex + 1 >= closeBraceIndex
                || tokens.get(keyIndex + 1).type() != TokenType.COLON
                || valStartIdx >= closeBraceIndex) {
            return keyIndex;
        }

        final int valEndIdx = findPropertyValueEnd(valStartIdx, closeBraceIndex);
        final Token firstValToken = tokens.get(valStartIdx);
        final Token lastValToken = tokens.get(Math.max(valStartIdx, valEndIdx - 1));
        final String valueText = source.subSequence(firstValToken.startOffset(), lastValToken.endOffset()).toString();

        final JsProperty prop = new JsProperty(
                unquoteKey(keyToken),
                keyToken.startOffset(),
                keyToken.endOffset(),
                firstValToken.startOffset(),
                lastValToken.endOffset(),
                valueText,
                null,
                owner
        );
        owner.addProperty(prop);
        allProperties.add(prop);

        if (firstValToken.type() == TokenType.LBRACE
                && findMatchingClose(valStartIdx, closeBraceIndex, TokenType.LBRACE, TokenType.RBRACE) == valEndIdx - 1) {
            prop.setValueObject(parseObjectLiteral(valStartIdx, valEndIdx - 1, prop, null));
        } else {
            parseBlock(valStartIdx, valEndIdx, prop);
        }
        return valEndIdx;
    }

    private int skipEntry(
            final int startIndex,
            final int limitIndex,
            final @Nullable JsProperty enclosingProperty
    ) {
        final int endIdx = findPropertyValueEnd(startIndex, limitIndex);
        parseBlock(startIndex, endIdx, enclosingProperty);
        return Math.max(startIndex + 1, endIdx);
    }

    private int findPropertyValueEnd(final int startIndex, final int closeBraceIndex) {
        int braceDepth = 0;
        int parenDepth = 0;
        int bracketDepth = 0;

        for (int i = startIndex; i < closeBraceIndex; i++) {
            switch (tokens.get(i).type()) {
                case LBRACE -> braceDepth++;
                case RBRACE -> {
                    if (braceDepth == 0) {
                        return i;
                    }
                    braceDepth--;
                }
                case LPAREN -> parenDepth++;
                case RPAREN -> parenDepth = Math.max(0, parenDepth - 1);
                case LBRACKET -> bracketDepth++;
                case RBRACKET -> bracketDepth = Math.max(0, bracketDepth - 1);
                case COMMA -> {
                    if (braceDepth == 0 && parenDepth == 0 && bracketDepth == 0) {
                        return i;
                    }
                }
                default -> { }
            }
        }
        return closeBraceIndex;
    }

    private boolean isObjectLiteralAt(final int lbraceIndex) {
        if (lbraceIndex > 0) {
            final Token prev = tokens.get(lbraceIndex - 1);
            if (prev.type() == TokenType.RPAREN
                    || (prev.type() == TokenType.PUNCTUATION && "=>".equals(prev.text()))
                    || isControlFlowKeyword(prev)) {
                return false;
            }
        }
        if (lbraceIndex + 1 < tokens.size() && tokens.get(lbraceIndex + 1).type() == TokenType.RBRACE) {
            return true;
        }
        if (lbraceIndex + 2 < tokens.size()) {
            final Token first = tokens.get(lbraceIndex + 1);
            final Token second = tokens.get(lbraceIndex + 2);
            if (isPropertyKeyToken(first) && (second.type() == TokenType.COLON || second.type() == TokenType.LPAREN)) {
                return true;
            }
        }
        if (lbraceIndex > 0) {
            final Token prev = tokens.get(lbraceIndex - 1);
            return switch (prev.type()) {
                case EQUALS, COLON, LPAREN, LBRACKET, COMMA -> true;
                case IDENTIFIER -> "return".equals(prev.text());
                default -> false;
            };
        }
        return false;
    }

    private int findMatchingClose(
            final int openIndex,
            final int maxIndex,
            final @NotNull TokenType openType,
            final @NotNull TokenType closeType
    ) {
        int depth = 0;
        for (int i = openIndex; i < maxIndex; i++) {
            final TokenType type = tokens.get(i).type();
            if (type == openType) {
                depth++;
            } else if (type == closeType) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return maxIndex - 1;
    }

    private static boolean isVarDeclarationKeyword(final @NotNull Token token) {
        return token.type() == TokenType.IDENTIFIER
                && ("var".equals(token.text()) || "let".equals(token.text()) || "const".equals(token.text()));
    }

    private static boolean isControlFlowKeyword(final @NotNull Token token) {
        return token.type() == TokenType.IDENTIFIER
                && ("try".equals(token.text())
                || "else".equals(token.text())
                || "finally".equals(token.text())
                || "do".equals(token.text()));
    }

    private static boolean isPropertyKeyToken(final @NotNull Token token) {
        return token.type() == TokenType.IDENTIFIER
                || token.type() == TokenType.STRING
                || token.type() == TokenType.NUMBER;
    }

    private static @NotNull String unquoteKey(final @NotNull Token token) {
        final String text = token.text();
        if (token.type() == TokenType.STRING && text.length() >= 2) {
            final char first = text.charAt(0);
            final char last = text.charAt(text.length() - 1);
            if ((first == '\'' && last == '\'')
                    || (first == '"' && last == '"')
                    || (first == '`' && last == '`')) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }
}
