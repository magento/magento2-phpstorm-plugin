/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class Plugin implements ModuleFileInterface {
    public static String TEMPLATE = "PHP Class";
    public static final String BEFORE_METHOD_TEMPLATE_NAME = "Magento Plugin Before Method";
    public static final String AROUND_METHOD_TEMPLATE_NAME = "Magento Plugin Around Method";
    public static final String AFTER_METHOD_TEMPLATE_NAME = "Magento Plugin After Method";

    public static enum PluginType {
        before,
        after,
        around
    }

    //forbidden target method
    public static final String constructMethodName = "__construct";

    //allowed methods access type
    public static final String publicAccess = "public";

    private static Plugin INSTANCE = null;
    private String fileName;

    public static Plugin getInstance(String className) {
        if (null == INSTANCE) {
            INSTANCE = new Plugin();
        }
        INSTANCE.setFileName(className.concat(".php"));
        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private void setFileName(String filename) {
        this.fileName = filename;
    };

    public static String getMethodTemplateByPluginType(PluginType pluginType)
    {
        if (pluginType.equals(PluginType.after)) {
            return AFTER_METHOD_TEMPLATE_NAME;
        }
        if (pluginType.equals(PluginType.before)) {
            return BEFORE_METHOD_TEMPLATE_NAME;
        }
        if (pluginType.equals(PluginType.around)) {
            return AROUND_METHOD_TEMPLATE_NAME;
        }
        return null;
    }

    public static Plugin.PluginType getPluginTypeByString(String string)
    {
        for (Plugin.PluginType pluginType: Plugin.PluginType.values()) {
            if (!pluginType.toString().equals(string))
            {
                continue;
            }
            return pluginType;
        }
        return null;
    }
}
