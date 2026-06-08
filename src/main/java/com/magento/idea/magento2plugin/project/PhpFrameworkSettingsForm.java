/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.frameworks.PhpFrameworkConfigurable;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

public class PhpFrameworkSettingsForm extends SettingsForm implements PhpFrameworkConfigurable {
    public PhpFrameworkSettingsForm(final @NotNull Project project) {
        super(project);
    }

    @Override
    public boolean isBeingUsed() {
        return isMagentoSupportEnabled();
    }

    @Override
    protected void afterSettingsApplied(final @NotNull Settings.State state) {
        ConfigurationManager.getInstance().refreshIncludePaths(state, getProject());
    }
}
