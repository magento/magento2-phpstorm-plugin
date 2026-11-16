/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import java.util.LinkedHashSet;
import java.util.List;

/** CLI defaults and normalization shared by desktop IDEs and the HCE host. */
public final class MagentoCliConfiguration {
    public static final String DEFAULT_CANDIDATES =
            "bin/magento, bin/n98-magerun2, bin/n98-magerun, bin/magerun, "
                    + "bin/cli, bin/console, bin/composer, bin/php";

    private MagentoCliConfiguration() { }

    public static List<String> parseCandidates(final String value) {
        final String raw = value == null || value.trim().isEmpty() ? DEFAULT_CANDIDATES : value;
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final String part : raw.split("[,;\\n\\r]+")) {
            String normalized = part.trim().replace('\\', '/');
            while (normalized.startsWith("./")) {
                normalized = normalized.substring(2);
            }
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return List.copyOf(result);
    }
}
