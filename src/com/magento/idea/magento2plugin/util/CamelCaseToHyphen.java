/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseToHyphen {
    private static CamelCaseToHyphen INSTANCE = null;

    /**
     * getInstance.
     * @return CamelCaseToHyphen
     */
    public static CamelCaseToHyphen getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CamelCaseToHyphen();
        }
        return INSTANCE;
    }

    /**
     * convert.
     * @param string the origin string.
     * @return string
     */
    public String convert(String string) {
        String regex = "(?=[A-Z][a-z])";
        String subst = "-";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        String result = matcher.replaceAll(subst).toLowerCase();
        if (result.charAt(0) == '-') {
            return result.substring(1);
        }
        return result;
    }
}
