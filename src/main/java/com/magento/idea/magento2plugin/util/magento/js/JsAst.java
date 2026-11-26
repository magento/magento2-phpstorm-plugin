/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight immutable index of JavaScript object literals, properties, and variable object assignments
 * parsed without the JetBrains JavaScript plugin.
 */
public final class JsAst {
    private final List<JsObject> objects;
    private final List<JsProperty> properties;
    private final Map<String, List<JsObject>> variableObjects;

    JsAst(
            final @NotNull List<JsObject> objects,
            final @NotNull List<JsProperty> properties,
            final @NotNull Map<String, List<JsObject>> variableObjects
    ) {
        this.objects = List.copyOf(objects);
        this.properties = List.copyOf(properties);
        this.variableObjects = Collections.unmodifiableMap(variableObjects);
    }

    public @NotNull List<JsObject> getObjects() {
        return objects;
    }

    public @NotNull List<JsProperty> getProperties() {
        return properties;
    }

    public @NotNull List<JsObject> getVariableObjects(final @NotNull String variableName) {
        final List<JsObject> found = variableObjects.get(variableName);
        return found == null ? Collections.emptyList() : Collections.unmodifiableList(found);
    }
}
