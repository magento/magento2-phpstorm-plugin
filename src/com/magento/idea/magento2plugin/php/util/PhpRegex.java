package com.magento.idea.magento2plugin.php.util;

public class PhpRegex {
    public static class Xml {
        public static final String CLASS_NAME
                = "\\\\?([A-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*(\\\\[A-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*)*)";

        public static final String CLASS_MEMBER_NAME = "::\\$?([a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*)(\\(\\))?";

        public static final String CLASS_ELEMENT = CLASS_NAME + "(" + CLASS_MEMBER_NAME + ")?.*";
    }
}
