/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.generation;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import icons.PhpIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.Icon;

public class MagentoTemplatesFactory extends ProjectTemplatesFactory {
    public MagentoTemplatesFactory() {
    }

    @NotNull
    public String[] getGroups() {
        return new String[]{"PHP"};
    }

    public Icon getGroupIcon(String group) {
        return PhpIcons.Php_icon;
    }

    @NotNull
    public ProjectTemplate[] createTemplates(@Nullable String group, WizardContext context) {
        return new ProjectTemplate[]{new MagentoModuleGenerator()};
    }
}
