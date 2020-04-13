/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

public class Regex {

    public static final String ALPHANUMERIC
            = "[a-zA-Z0-9]*";

    public static final String NUMERIC
            = "[0-9]*";

    public static final String IDENTIFIER
            = "[a-zA-Z0-9_\\-]*";

    public static final String DIRECTORY
            = "^(?!\\/)[a-zA-Z0-9\\/]*[^\\/]$";

    public static final String MFTF_CURLY_BRACES
            = ".*\\{\\{[^\\}]+\\}\\}.*";
}
