/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parsed property entry inside a {@link JsObject}.
 */
public final class JsProperty {
    private final String name;
    private final int keyStartOffset;
    private final int keyEndOffset;
    private final int valueStartOffset;
    private final int valueEndOffset;
    private final @Nullable String rawValueText;
    private final JsObject containingObject;
    private @Nullable JsObject valueObject;

    JsProperty(
            final @NotNull String name,
            final int keyStartOffset,
            final int keyEndOffset,
            final int valueStartOffset,
            final int valueEndOffset,
            final @Nullable String rawValueText,
            final @Nullable JsObject valueObject,
            final @NotNull JsObject containingObject
    ) {
        this.name = name;
        this.keyStartOffset = keyStartOffset;
        this.keyEndOffset = keyEndOffset;
        this.valueStartOffset = valueStartOffset;
        this.valueEndOffset = valueEndOffset;
        this.rawValueText = rawValueText;
        this.valueObject = valueObject;
        this.containingObject = containingObject;
    }

    void setValueObject(final @Nullable JsObject valueObject) {
        this.valueObject = valueObject;
    }

    public @NotNull String getName() {
        return name;
    }

    public int getKeyStartOffset() {
        return keyStartOffset;
    }

    public int getKeyEndOffset() {
        return keyEndOffset;
    }

    public int getValueStartOffset() {
        return valueStartOffset;
    }

    public int getValueEndOffset() {
        return valueEndOffset;
    }

    public @Nullable String getRawValueText() {
        return rawValueText;
    }

    public @Nullable String getStringValue() {
        if (valueObject != null || rawValueText == null) {
            return null;
        }
        return unquoteOrTrim(rawValueText);
    }

    public @Nullable JsObject getValueObject() {
        return valueObject;
    }

    public @NotNull JsObject getContainingObject() {
        return containingObject;
    }

    public @Nullable JsProperty getParentProperty() {
        return containingObject.getParentProperty();
    }

    static @Nullable String unquoteOrTrim(final @Nullable String rawValue) {
        if (rawValue == null) {
            return null;
        }
        final String trimmed = rawValue.trim();
        if (trimmed.length() >= 2) {
            final char first = trimmed.charAt(0);
            final char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        return normalizeRequirePath(trimmed);
    }

    static @Nullable String normalizeRequirePath(final @Nullable String path) {
        if (path == null) {
            return null;
        }
        final String normalized = path.replace("\"", "").replace("'", "").trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
