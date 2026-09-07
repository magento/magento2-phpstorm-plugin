/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.area;

import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AreaResolverUtil {

    private static final String CUSTOM_THEME_AREA = "\\/design\\/(adminhtml|frontend)\\/";
    private static final String MODULE_AREA =
            "\\/view\\/(adminhtml|frontend|base|crontab|webapi_rest|webapi_soap|graphql)\\/";

    private AreaResolverUtil() {}

    /**
     * Get Magento 2 area for the specified file (file should be in the custom theme).
     *
     * @param virtualFile VirtualFile
     *
     * @return Areas or null if file does not belong to the custom (editable) theme.
     */
    public static @Nullable Areas getForFileInCustomTheme(final @NotNull VirtualFile virtualFile) {
        return getArea(virtualFile.getPath(), CUSTOM_THEME_AREA);
    }

    /**
     * Get Magento 2 area for the specified file (file should be in the Magento 2 module).
     *
     * @param virtualFile VirtualFile
     *
     * @return Areas or null if file does not belong to the Magento 2 module.
     */
    public static @Nullable Areas getForFileInModule(final @NotNull VirtualFile virtualFile) {
        return getArea(virtualFile.getPath(), MODULE_AREA);
    }

    private static @Nullable Areas getArea(
            final @NotNull String filePath,
            final @NotNull String searchingRegex
    ) {
        final Pattern pattern = Pattern.compile(searchingRegex);
        final Matcher matcher = pattern.matcher(filePath);
        String areaName = null;

        if (matcher.find()) {
            areaName =  matcher.group(1);
        }

        return areaName == null ? null : Areas.getAreaByString(areaName);
    }
}
