/*
 * @author Atwix Team
 * @copyright Copyright (c) 2020 Atwix (https://www.atwix.com/)
 */

package com.magento.idea.magento2plugin.inspections.php.plugin.inspection;

import com.jetbrains.php.lang.psi.elements.Method;

public abstract class AbstractPluginInspector implements PluginInspector {
    private String pluginPrefix;
    private Method pluginMethod;
    public int minimumArguments = 2;

    public AbstractPluginInspector(String pluginPrefix, Method pluginMethod) {
        this.pluginPrefix = pluginPrefix;
        this.pluginMethod = pluginMethod;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public Method getPluginMethod() {
        return pluginMethod;
    }

    public int getMinimumPluginArguments() {
        return minimumArguments;
    }

    public String getTargetMethodName() {
        String pluginMethodName = getPluginMethod().getName();
        String targetClassMethodName = pluginMethodName.
                replace(getPluginPrefix(), "");
        return Character.toLowerCase(targetClassMethodName.charAt(0)) + targetClassMethodName.substring(1);
    }

    @Override
    public boolean inspectMaximumArguments(int targetMethodArgumentsCount) {
        int pluginArgumentsLength = getPluginMethodArgumentsCount();
        return !(pluginArgumentsLength > targetMethodArgumentsCount + this.getMinimumPluginArguments());
    }

    @Override
    public boolean inspectMinimumArguments(int targetMethodArgumentsCount) {
        int pluginArgumentsLength = getPluginMethodArgumentsCount();
        return !(pluginArgumentsLength < targetMethodArgumentsCount + this.getMinimumPluginArguments());
    }

    protected int getPluginMethodArgumentsCount() {
        return getPluginMethod().getParameters().length;
    }
}
