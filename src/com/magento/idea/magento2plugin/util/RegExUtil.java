/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

public class RegExUtil {

    public static final String FILE_PATH = "[\\w-]+(/[\\w-]*)*";

    public static class Magento {
        public static final String MODULE_NAME
                = "[A-Z][a-zA-Z0-9]+_[A-Z][a-zA-Z0-9]+";
    }
}
