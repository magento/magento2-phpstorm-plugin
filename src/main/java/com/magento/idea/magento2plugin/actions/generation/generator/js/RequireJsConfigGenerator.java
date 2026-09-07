/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.js;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.generator.FileGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.magento.files.RequireConfigJsFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;


public class RequireJsConfigGenerator extends FileGenerator {

    private final PsiDirectory directory;

    public RequireJsConfigGenerator(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super(project);
        this.directory = directory;
    }

    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final RequireConfigJsFile file = new RequireConfigJsFile();
        PsiFile configFile = directory.findFile(file.getFileName());

        if (configFile == null) {
            final FileFromTemplateGenerator generator = new FileFromTemplateGenerator(project);
            configFile = generator.generate(
                    file,
                    new Properties(),
                    directory,
                    actionName
            );
        }

        return configFile;
    }

    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    @Override
    protected void fillAttributes(final Properties attributes) {}
}
