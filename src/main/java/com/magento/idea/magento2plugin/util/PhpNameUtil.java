/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import com.intellij.openapi.util.text.StringUtil;
import com.magento.idea.magento2plugin.magento.packages.code.FrameworkLibraryType;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Normalizes class names stored in configuration without requiring PHP PSI.
 */
public final class PhpNameUtil {
    private static final String PROXY_SUFFIX = "\\Proxy";

    private PhpNameUtil() {
    }

    public static @NotNull String toPresentableFQN(final @NotNull String name) {
        return StringUtil.trimStart(name, "\\");
    }

    public static @NotNull String toFQN(final @NotNull String name) {
        return name.startsWith("\\") ? name : "\\" + name;
    }

    /**
     * Get the classes a configured object type can refer to: the type as written and, for a
     * generated proxy or factory name, the classes it is generated from.
     *
     * @param type String
     *
     * @return class names, the written type first
     */
    public static @NotNull Set<String> getObjectTypeCandidates(final @NotNull String type) {
        final String withoutProxy = StringUtil.trimEnd(type, PROXY_SUFFIX);
        final String source = StringUtil.trimEnd(withoutProxy, FrameworkLibraryType.FACTORY_SUFFIX);
        final Set<String> candidates = new LinkedHashSet<>();

        for (final String name : List.of(type, withoutProxy, source)) {
            if (!name.isEmpty() && !name.endsWith("\\")) {
                candidates.add(name);
            }
        }

        return candidates;
    }
}
