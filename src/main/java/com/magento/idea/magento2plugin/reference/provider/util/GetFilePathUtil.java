/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFilePathUtil {

    private static GetFilePathUtil INSTANCE;

    public static GetFilePathUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetFilePathUtil();
        }
        return INSTANCE;
    }

    public String execute(@NotNull String value)
    {
        String moduleName = GetModuleNameUtil.getInstance().execute(value);
        if (null != moduleName && value.contains(moduleName))  {
            value = value.replace(moduleName, "");
        }

        Pattern pattern = Pattern.compile("\\W?(([\\w-]+/)*[\\w\\.-]+)");
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }
}
