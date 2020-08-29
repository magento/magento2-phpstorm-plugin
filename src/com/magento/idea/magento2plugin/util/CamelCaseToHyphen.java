/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

public class CamelCaseToHyphen {
    private static CamelCaseToHyphen INSTANCE = null;
    public static CamelCaseToHyphen getInstance() {
        if (null == INSTANCE) INSTANCE = new CamelCaseToHyphen();
        return INSTANCE;
    }

    public String convert(String string) {
        return string.toLowerCase();
    }
}
