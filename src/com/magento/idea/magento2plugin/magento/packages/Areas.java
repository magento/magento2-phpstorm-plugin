/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

@SuppressWarnings({"PMD.FieldNamingConventions"})
public enum Areas {
    base,
    adminhtml,
    frontend,
    crontab,
    webapi_rest,
    webapi_soap,
    graphql;

    /**
     * Casts string to the certain area.
     *
     * @param string String
     * @return Areas
     */
    public static Areas getAreaByString(final String string) {
        Areas result = null;
        for (final Areas area: values()) {
            if (!area.toString().equals(string)) {
                continue;
            }

            result = area;
        }

        return result;
    }
}
