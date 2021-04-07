/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.database;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes;
import org.jetbrains.annotations.NotNull;

public final class PropertyToDefaultTypeMapperUtil {

    private PropertyToDefaultTypeMapperUtil() {}

    /**
     * Get default database type for specified entity property.
     *
     * @param propertyType PropertiesTypes
     *
     * @return TableColumnTypes
     */
    public static TableColumnTypes map(final @NotNull PropertiesTypes propertyType) {
        switch (propertyType) {
            case INT:
                return TableColumnTypes.INT;
            case BOOL:
                return TableColumnTypes.BOOLEAN;
            case FLOAT:
                return TableColumnTypes.DECIMAL;
            case STRING:
                return TableColumnTypes.VARCHAR;
            default:
                throw new NotSupportedException(
                        "ENUMs " + PropertiesTypes.class + " property is not supported."
                );
        }
    }
}
