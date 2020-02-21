/*
 * @author Atwix Team
 * @copyright Copyright (c) 2020 Atwix (https://www.atwix.com/)
 */

package com.magento.idea.magento2plugin.inspections.php.plugin.inspection;

import com.jetbrains.php.lang.psi.elements.Method;

public class PluginInspectorFactory {
    public static final String aroundPluginPrefix = "around";
    public static final String beforePluginPrefix = "before";
    public static final String afterPluginPrefix = "after";

    public static PluginInspector create(Method pluginMethod) {
        String pluginMethodName = pluginMethod.getName();
        if (pluginMethodName.startsWith(aroundPluginPrefix)) {
            return new AroundPluginInspector(aroundPluginPrefix, pluginMethod);
        }
        if (pluginMethodName.startsWith(beforePluginPrefix)) {
            return new BeforePluginInspector(beforePluginPrefix, pluginMethod);
        }
        if (pluginMethodName.startsWith(afterPluginPrefix)) {
            return new AfterPluginInspector(afterPluginPrefix, pluginMethod);
        }

        return null;
    }
}
