/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

public class RegExUtil {

    public static final String FILE_PATH = "[\\w-]+(/[\\w-]*)*";

    public static final String ALPHANUMERIC
            = "[a-zA-Z0-9]*";

    public static final String NUMERIC
            = "[0-9]*";

    public static final String IDENTIFIER
            = "[a-zA-Z0-9_\\-]*";

    public static final String DIRECTORY
            = "^(?!\\/)[a-zA-Z0-9\\/]*[^\\/]$";

    public static final String MAGENTO_VERSION
            = "(\\d+)\\.(\\d+)\\.(\\d+)";

    public static class Magento {
        public static final String MODULE_NAME
                = "[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+";
    }

    public class PhpRegex {

        public static final String CLASS_NAME
                = "[A-Z][a-zA-Z0-9_\\x7f-\\xff]*";

        public static final String FQN
                = CLASS_NAME + "(\\\\" + CLASS_NAME + ")*";
    }

    public static class XmlRegex {

        public static final String CLASS_MEMBER_NAME =
                "::\\$?([a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*)(\\(\\))?";

        public static final String CLASS_ELEMENT =
                "\\\\?" + PhpRegex.FQN + "(" + CLASS_MEMBER_NAME + ")?.*";
    }
}
