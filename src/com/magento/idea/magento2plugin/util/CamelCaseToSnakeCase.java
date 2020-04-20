/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

public class CamelCaseToSnakeCase {
    private static CamelCaseToSnakeCase INSTANCE = null;

    public static CamelCaseToSnakeCase getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CamelCaseToSnakeCase();
        }
        return INSTANCE;
    }

    /**
     * Convert camelCaseString to snake_case_string
     *
     * @param camelCaseString
     *
     * @return String
     */
    public String convert(String camelCaseString) {
        return camelCaseString.replaceAll("\\B([A-Z])", "_$1").toLowerCase();
    }
}
