/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.util.Locale;

public final class FirstLetterToLowercaseUtil {

    private FirstLetterToLowercaseUtil() {}

    /**
     * Converts first letter of a string to lowercase.
     * Eg: "FoO" -> "foO".
     *
     * @param string String
     * @return String
     */
    public static String convert(final String string) {
        if (string == null || string.length() == 0) {
            return "";
        }

        final int minLength = 1;
        if (string.length() == minLength) {
            return string.toLowerCase(Locale.ROOT);
        }

        char[] chArr = string.toCharArray();
        chArr[0] = Character.toLowerCase(chArr[0]);

        return new String(chArr);
    }
}
