/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class Plugin implements ModuleFileInterface {
    private static final String BEFORE_METHOD_TEMPLATE_NAME = "Magento Plugin Before Method";
    private static final String AROUND_METHOD_TEMPLATE_NAME = "Magento Plugin Around Method";
    private static final String AFTER_METHOD_TEMPLATE_NAME = "Magento Plugin After Method";

    public static final String CALLABLE_PARAM = "callable";
    public static final String CLOSURE_PARAM = "Closure";
    public static final String NON_INTERCEPTABLE_FQN
            = "\\Magento\\Framework\\ObjectManager\\NoninterceptableInterface";

    public enum PluginType {
        before,//NOPMD
        after,//NOPMD
        around//NOPMD
    }

    //forbidden target method
    public static final String CONSTRUCT_METHOD_NAME = "__construct";

    //allowed methods access type
    public static final String PUBLIC_ACCESS = "public";

    private String fileName;

    /**
     * Constructor.
     *
     * @param className String
     */
    public Plugin(final String className) {
        this.setFileName(className.concat(".php"));
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getTemplate() {
        return "PHP Class";
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    private void setFileName(final String filename) {
        this.fileName = filename;
    }

    /**
     * Get Method Template.
     *
     * @param pluginType PluginType
     * @return String
     */
    public static String getMethodTemplateByPluginType(final PluginType pluginType) {
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

    /**
     * Get Plugin Type.
     *
     * @param string String
     * @return Plugin.PluginType
     */
    public static Plugin.PluginType getPluginTypeByString(final String string) {
        for (final Plugin.PluginType pluginType: Plugin.PluginType.values()) {
            if (pluginType.toString().equals(string)) {
                return pluginType;
            }
        }
        return null;
    }
}
