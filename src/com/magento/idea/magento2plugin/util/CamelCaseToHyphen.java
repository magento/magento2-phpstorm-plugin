/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

import java.lang.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseToHyphen {
    private static CamelCaseToHyphen INSTANCE = null;
    public static CamelCaseToHyphen getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CamelCaseToHyphen();
        }
        return INSTANCE;
    }

    public String convert(String string) {
        return string.toLowerCase();
    }
}
