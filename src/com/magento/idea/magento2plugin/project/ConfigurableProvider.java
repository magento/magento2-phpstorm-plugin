/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.frameworks.PhpFrameworkConfigurable;
import com.jetbrains.php.frameworks.PhpFrameworkConfigurableProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ConfigurableProvider implements PhpFrameworkConfigurableProvider {
    public ConfigurableProvider() {
    }

    @Nls
    @NotNull
    public String getName() {
        return "Magento 2";
    }

    @NotNull
    public PhpFrameworkConfigurable createConfigurable(@NotNull Project project) {
        return new SettingsForm(project);
    }
}
