/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.scanner.filter;

import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExcludeMagentoBundledFilter implements ModuleScannerFilter {

    private static final List<String> MAGENTO_BUNDLED_VENDORS = new ArrayList<>(
            Arrays.asList(
                    "magento",
                    "amazon",
                    "paypal",
                    "dotdigitalgroup",
                    "klarna",
                    "vertex",
                    "yotpo",
                    "fastly"
            )
    );

    @Override
    public boolean isExcluded(final ComponentData component) {
        final String vendorName = extractVendorFromModuleName(component.getName());

        if (vendorName == null) {
            return false;
        }

        return MAGENTO_BUNDLED_VENDORS.contains(vendorName);
    }

    /**
     * Extract vendor name from module name.
     *
     * @param moduleName String
     *
     * @return String
     */
    private @Nullable String extractVendorFromModuleName(final @NotNull String moduleName) {
        String[] moduleNamePats = moduleName.split("_");

        if (moduleNamePats.length > 1) { //NOPMD
            return moduleNamePats[0].toLowerCase(Locale.ROOT);
        }
        moduleNamePats = moduleName.split("/");

        return moduleNamePats.length > 1 ? moduleNamePats[0].toLowerCase(Locale.ROOT) : null;
    }
}
