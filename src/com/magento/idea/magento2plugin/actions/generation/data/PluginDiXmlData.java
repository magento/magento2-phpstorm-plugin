/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class PluginDiXmlData {
    private String area;
    private String pluginModule;
    private PhpClass targetClass;
    private final String sortOrder;
    private final String pluginName;
    private String pluginFqn;

    public PluginDiXmlData(
            String area,
            String pluginModule,
            PhpClass targetClass,
            String sortOrder,
            String pluginName,
            String pluginFqn
    ) {
        this.area = area;
        this.pluginModule = pluginModule;
        this.targetClass = targetClass;
        this.sortOrder = sortOrder;
        this.pluginName = pluginName;
        this.pluginFqn = pluginFqn;
    }

    public String getArea() {
        return area;
    }

    public String getPluginModule() {
        return pluginModule;
    }

    public PhpClass getTargetClass() {
        return targetClass;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public String getPluginFqn() {
        return pluginFqn;
    }
}
