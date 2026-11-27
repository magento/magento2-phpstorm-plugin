/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parsed JavaScript object literal node and its contained properties.
 */
public final class JsObject {
    private final int startOffset;
    private final int endOffset;
    private final @Nullable JsProperty parentProperty;
    private final @Nullable String assignedVariableName;
    private final List<JsProperty> properties = new ArrayList<>();
    private final Map<String, JsProperty> propertiesByName = new LinkedHashMap<>();

    JsObject(
            final int startOffset,
            final int endOffset,
            final @Nullable JsProperty parentProperty,
            final @Nullable String assignedVariableName
    ) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.parentProperty = parentProperty;
        this.assignedVariableName = assignedVariableName;
    }

    void addProperty(final @NotNull JsProperty property) {
        properties.add(property);
        propertiesByName.put(property.getName(), property);
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public @Nullable JsProperty getParentProperty() {
        return parentProperty;
    }

    public @Nullable String getAssignedVariableName() {
        return assignedVariableName;
    }

    public @NotNull List<JsProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public @Nullable JsProperty findProperty(final @NotNull String name) {
        return propertiesByName.get(name);
    }
}
