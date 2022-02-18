/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class GetAttributeOptionPropertiesUtil {
    public static final String OPTION_VALUE = "Value";
    public static final String OPTION_SORT_ORDER = "Sort Order";

    private GetAttributeOptionPropertiesUtil() {}

    /**
     * Returns sort orders for options.
     *
     * @param columnsData List
     */
    public static Map<Integer, String> getSortOrders(
            @NotNull final List<Map<String, String>> columnsData
    ) {
        final Map<Integer, String> sortOrders = new HashMap<>();

        for (int i = 0; i < columnsData.size(); i++) {
            final String sortOrder = columnsData.get(i).get(OPTION_SORT_ORDER);
            if (!sortOrder.isEmpty()) {
                sortOrders.put(i, sortOrder);
            }
        }

        return sortOrders;
    }

    /**
     * Returns options values.
     *
     * @param columnsData List
     */
    public static Map<Integer, String> getValues(
            @NotNull final List<Map<String, String>> columnsData
    ) {
        final Map<Integer, String> options = new HashMap<>();

        for (int i = 0; i < columnsData.size(); i++) {
            options.put(i, columnsData.get(i).get(OPTION_VALUE));
        }

        return options;
    }
}
