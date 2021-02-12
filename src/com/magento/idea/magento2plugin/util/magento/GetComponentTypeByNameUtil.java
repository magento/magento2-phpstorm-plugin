/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public final class GetComponentTypeByNameUtil {

    private GetComponentTypeByNameUtil(){}

    /**
     * Returns component type by component name.
     *
     * @param componentName String
     * @return String
     */
    @Nullable
    public static String execute(final String componentName) {
        String componentType = null;
        if (isModule(componentName)) {
            componentType = ComponentType.module.toString();
        } else if (isTheme(componentName)) {
            componentType = ComponentType.theme.toString();
        }

        return componentType;
    }

    private static boolean isModule(final String componentName) {
        final Pattern modulePattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        final Matcher moduleMatcher = modulePattern.matcher(componentName);

        return moduleMatcher.find();
    }

    private static boolean isTheme(final String componentName) {
        final Pattern themePattern = Pattern.compile(RegExUtil.Magento.THEME_NAME);
        final Matcher themeMatcher = themePattern.matcher(componentName);

        return themeMatcher.find();
    }
}
