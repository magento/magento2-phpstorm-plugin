/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.configurations;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UctConfigurationFactory extends ConfigurationFactory {

    /**
     * UCT configuration factory constructor.
     *
     * @param type ConfigurationType
     */
    protected UctConfigurationFactory(final @NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return UctRunConfigurationType.RUN_CONFIGURATION_TYPE_ID;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(final @NotNull Project project) {
        return new UctRunConfiguration(project, this, UctRunConfigurationType.TITLE);
    }

    @Override
    public @Nullable Class<? extends BaseState> getOptionsClass() {
        return UctRunConfigurationOptions.class;
    }
}
