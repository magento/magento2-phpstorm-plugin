/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

public class MagentoPluginFileData {
    private String pluginDirectory;
    private String pluginClassName;
    private String pluginType;
    private String pluginModule;
    private PhpClass targetClass;
    private Method targetMethod;

    public MagentoPluginFileData(
            String pluginDirectory,
            String pluginClassName,
            String pluginType,
            String pluginModule,
            PhpClass targetClass,
            Method targetMethod
    ) {
        this.pluginDirectory = pluginDirectory;
        this.pluginClassName = pluginClassName;
        this.pluginType = pluginType;
        this.pluginModule = pluginModule;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }

    public String getPluginClassName() {
        return pluginClassName;
    }

    public String getPluginDirectory() {
        return pluginDirectory;
    }

    public String getPluginType() {
        return pluginType;
    }

    public String getPluginModule() {
        return pluginModule;
    }

    public PhpClass getTargetClass() {
        return targetClass;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }
}
