/*
 * @author Atwix Team
 * @copyright Copyright (c) 2020 Atwix (https://www.atwix.com/)
 */

package com.magento.idea.magento2plugin.inspections.php.plugin.inspection;

import com.jetbrains.php.lang.psi.elements.Method;

public class BeforePluginInspector extends AbstractPluginInspector {

    public BeforePluginInspector(String pluginPrefix, Method pluginMethod) {
        super(pluginPrefix, pluginMethod);
    }

    @Override
    public int getMinimumPluginArguments() {
        return 1;
    }
}
