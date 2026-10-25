/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.php;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.refactoring.PhpNameUtil;
import org.jetbrains.annotations.NotNull;

/** PHP-plugin-backed validation shared by generation rules. */
public final class PhpNameValidator {

    private PhpNameValidator() {}

    public static boolean isValidClassName(final @NotNull String value) {
        return PhpNameUtil.isValidClassName(value);
    }

    public static boolean isValidNamespace(final @NotNull String value) {
        return PhpNameUtil.isValidNamespaceFullName(value, PhpLanguageLevel.DEFAULT);
    }
}
