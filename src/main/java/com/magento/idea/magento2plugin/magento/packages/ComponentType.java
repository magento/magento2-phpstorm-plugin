/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.FieldNamingConventions"})
public enum ComponentType {
    module,
    theme,
    library;

    /**
     * Get component type by value.
     *
     * @param value String
     *
     * @return ComponentType
     */
    public static ComponentType getByValue(final @NotNull String value) {
        for (final ComponentType type : ComponentType.values()) {
            if (value.equals(type.toString())) {
                return type;
            }
        }

        return null;
    }
}
