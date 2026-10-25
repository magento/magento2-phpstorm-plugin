/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import com.jetbrains.php.lang.PhpLangUtil;
import junit.framework.TestCase;

public class PhpNameUtilTest extends TestCase {
    public void testConfigurationNamesPreservePhpNormalization() {
        final String[] names = {
                "", "\\", "\\\\", "Vendor\\Module\\Model", "\\Vendor\\Module\\Model",
                "\\\\Vendor\\Module\\Model", "VirtualTypeAlias", " Vendor\\Module ",
                "Vendor\\Module\\", "vendor\\MODULE"
        };
        for (final String name : names) {
            assertEquals(name, PhpLangUtil.toPresentableFQN(name), PhpNameUtil.toPresentableFQN(name));
            assertEquals(name, PhpLangUtil.toFQN(name), PhpNameUtil.toFQN(name));
        }
    }
}
