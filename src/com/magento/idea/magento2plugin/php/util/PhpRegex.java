/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.php.util;

public class PhpRegex {

    public static final String CLASS_NAME
            = "[A-Z][a-zA-Z0-9_\\x7f-\\xff]*";

    public static final String FQN
            = CLASS_NAME + "(\\\\" + CLASS_NAME + ")*";

    public static class Xml {

        public static final String CLASS_MEMBER_NAME = "::\\$?([a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*)(\\(\\))?";

        public static final String CLASS_ELEMENT = "\\\\?" + PhpRegex.FQN + "(" + CLASS_MEMBER_NAME + ")?.*";
    }
}
