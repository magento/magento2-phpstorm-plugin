/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.magento;

import com.magento.idea.magento2plugin.util.RegExUtil;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetComponentTypeByName {

    @Nullable
    public static String execute(String componentName) {
        String componentType = null;
        if (isModule(componentName)) {
            componentType = ComponentType.TYPE_MODULE;
        } else if (isTheme(componentName)) {
            componentType = ComponentType.TYPE_THEME;
        }

        return componentType;
    }

    private static boolean isModule(String componentName) {
        Pattern modulePattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        Matcher moduleMatcher = modulePattern.matcher(componentName);

        return moduleMatcher.find();
    }

    private static boolean isTheme(String componentName) {
        Pattern themePattern = Pattern.compile(RegExUtil.Magento.THEME_NAME);
        Matcher themeMatcher = themePattern.matcher(componentName);

        return themeMatcher.find();
    }
}
