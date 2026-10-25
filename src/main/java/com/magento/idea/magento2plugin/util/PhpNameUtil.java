/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Normalizes class names stored in configuration without requiring PHP PSI.
 */
public final class PhpNameUtil {
    private PhpNameUtil() {
    }

    public static @NotNull String toPresentableFQN(final @NotNull String name) {
        return StringUtil.trimStart(name, "\\");
    }

    public static @NotNull String toFQN(final @NotNull String name) {
        return name.startsWith("\\") ? name : "\\" + name;
    }
}
