/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({
        "PMD.ClassNamingConventions",
        "PMD.NonThreadSafeSingleton"
})
public class CamelCaseToHyphen {
    private static CamelCaseToHyphen instance;

    /**
     * Get Singleton.
     *
     * @return CamelCaseToHyphen
     */
    public static CamelCaseToHyphen getInstance() {
        if (null == instance) {
            instance = new CamelCaseToHyphen();
        }
        return instance;
    }

    /**
     * Converts camel case to hyphen.
     * e.g. "TestString" -> "test-string"
     *
     * @param string the origin string.
     * @return string
     */
    public String convert(final String string) {
        final String regex = "(?=[A-Z][a-z])";
        final String subst = "-";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        final String result =
                matcher.replaceAll(subst).toLowerCase(new Locale("en","EN"));
        final char hyphenSeparator = '-';
        if (result.charAt(0) == hyphenSeparator) {
            return result.substring(1);
        }
        return result;
    }
}
