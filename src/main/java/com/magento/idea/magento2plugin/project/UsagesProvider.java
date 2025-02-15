/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.frameworks.PhpFrameworkUsageProvider;
import org.jetbrains.annotations.NotNull;

public class UsagesProvider implements PhpFrameworkUsageProvider {
    public UsagesProvider() {
    }

    @NotNull
    public String getName() {
        return "Magento 2";
    }

    public boolean isEnabled(@NotNull Project project) {
        return Settings.getInstance(project).pluginEnabled;
    }
}
