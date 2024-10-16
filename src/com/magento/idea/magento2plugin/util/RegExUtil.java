/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

@SuppressWarnings({"PMD.ClassNamingConventions"})
public class RegExUtil {

    public static final String FILE_PATH = "[\\w-]+(/[\\w-]*)*";

    public static final String ALPHANUMERIC
            = "[a-zA-Z0-9]*";

    public static final String ALPHANUMERIC_WITH_UNDERSCORE
            = "[a-zA-Z0-9_]*";

    public static final String ALPHA_WITH_PERIOD
            = "[a-zA-Z.]*";

    public static final String ALPHA_WITH_DASH
            = "[a-zA-Z-]*";

    public static final String NUMERIC
            = "[0-9]*";

    public static final String EXTENDED_NUMERIC
            = "-?\\d+(\\.\\d+)?";

    public static final String IDENTIFIER
            = "[a-zA-Z0-9_\\-]*";

    public static final String IDENTIFIER_WITH_COLON
            = "[a-zA-Z0-9:_\\-]*";

    public static final String IDENTIFIER_WITH_SLASH
            = "[a-zA-Z0-9_\\-\\/]*";

    public static final String LOWER_SNAKE_CASE
            = "[a-z][a-z0-9_]*";

    public static final String CLI_COMMAND_NAME
            = "[a-zA-Z0-9_\\-\\:]*";

    public static final String DIRECTORY
            = "^(?!\\/)[a-zA-Z0-9\\/]*[^\\/]$";

    public static final String MAGENTO_VERSION
            = "(\\d+)\\.(\\d+)\\.(\\d+)[a-zA-Z0-9_\\-]*";

    public static final String LAYOUT_NAME
            = "^([a-zA-Z0-9]+){1}(_[a-zA-Z0-9]+){0,}";

    public static class Magento {

        public static final String PHP_CLASS
                = "[A-Z][a-zA-Z0-9]+";

        public static final String PHP_CLASS_FQN
                = "(" + PHP_CLASS + ")?(\\" + PHP_CLASS + ")+";

        public static final String MODULE_NAME
                = "[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+";

        public static final String THEME_NAME
                = "[a-z]+/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+";

        public static final String MFTF_CURLY_BRACES
                = ".*\\{\\{[^\\}]+\\}\\}.*";

        public static final String CRON_SCHEDULE =
                "^((\\*|\\?|\\d+((\\/|\\-){0,1}(\\d+))*)\\s*){5}$";

        public static final String CONFIG_PATH = "^(.+)\\/(.+)\\/(.+)$";

        public static final String ROUTE_ID = "^[A-Za-z0-9_]{3,}$";

        public static final String XML_IDENTIFIER =
                "^([A-Z]+[a-zA-Z0-9]{1,}){1,}_[A-Z]+[A-Z0-9a-z]{1,}::[A-Za-z_0-9]{1,}$";

        public static final String TEST_FILE_NAME = "\\w*Test.php";

        public static final String TEST_CLASS_FQN =
                "^(\\\\)?(\\w+\\\\){1}(\\w+\\\\){1}Test(\\\\\\w+)+$";

        public static final String CUSTOM_VENDOR_NAME =
                "app\\/code\\/(\\w+)\\/";

        public static final String COMMA_SEPARATED_STRING =
                "^[^\\s,]+(?:,\\s*[^\\s,]+)*$";
    }

    public static class PhpRegex {

        public static final String CLASS_NAME
                = "[a-zA-Z0-9_\\x7f-\\xff]*";

        public static final String FQN
                = CLASS_NAME + "(\\\\" + CLASS_NAME + ")*";
    }

    public static class XmlRegex {

        public static final String CLASS_MEMBER_NAME =
                "::\\$?([a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*)(\\(\\))?";

        public static final String CLASS_ELEMENT =
                "\\\\?" + PhpRegex.FQN + "(" + CLASS_MEMBER_NAME + ")?.*";
    }

    public static class JsRegex {

        // Targets paths like `'Sandbox_Test/js/test'`
        public static final String FILE_PATH
                = "(\\W{1}[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+[\\/\\w*-]{1,}\\W{1})";
    }

    public static class CustomTheme {

        public static final String MODULE_NAME =
                "app\\/design\\/(adminhtml|frontend)\\/\\w*\\/\\w*\\/\\w*";
    }
}
