/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetModuleNameUtil {

    private static GetModuleNameUtil INSTANCE;

    public static GetModuleNameUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetModuleNameUtil();
        }
        return INSTANCE;
    }

    public String execute(@NotNull String fileName)
    {
        Pattern pattern = Pattern.compile("(([A-Z][a-zA-Z0-9]+)_([A-Z][a-zA-Z0-9]+))");
        Matcher matcher = pattern.matcher(fileName);
        return matcher.find() ? matcher.group(1) : null;
    }
}
