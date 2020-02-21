/*
 * @author Atwix Team
 * @copyright Copyright (c) 2020 Atwix (https://www.atwix.com/)
 */

package com.magento.idea.magento2plugin.inspections.php.plugin.inspection;

public interface PluginInspector {

    public boolean inspectMinimumArguments(int targetArgumentsLength);

    public boolean inspectMaximumArguments(int targetArgumentsLength);

    public String getTargetMethodName();
}
