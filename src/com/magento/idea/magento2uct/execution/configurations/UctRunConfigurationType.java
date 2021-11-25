/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.configurations;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.magento.idea.magento2plugin.MagentoIcons;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class UctRunConfigurationType implements ConfigurationType {

    public static final String RUN_CONFIGURATION_TYPE_ID = "UctRunConfigurationType";
    public static final String TITLE = "Upgrade Compatibility Tool";
    public static final String SHORT_TITLE = "UCT Run";
    private static final String DESCRIPTION = "Magento 2 Upgrade Compatibility Tool Configuration";

    @Override
    public @NotNull String getDisplayName() {
        return TITLE;
    }

    @Override
    public String getConfigurationTypeDescription() {
        return DESCRIPTION;
    }

    @Override
    public Icon getIcon() {
        return MagentoIcons.PLUGIN_ICON_SMALL;
    }

    @Override
    public @NotNull String getId() {
        return RUN_CONFIGURATION_TYPE_ID;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new UctConfigurationFactory(this)};
    }
}
