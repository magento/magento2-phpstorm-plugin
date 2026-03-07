/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.util.NavigateToCreatedFile;
import java.util.Properties;

public abstract class FileGenerator {

    protected final Project project;
    protected final NavigateToCreatedFile navigateToCreatedFile;

    public FileGenerator(final Project project) {
        this.project = project;
        this.navigateToCreatedFile = NavigateToCreatedFile.getInstance();
    }

    /**
     * Generate target file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    public final PsiFile generate(final String actionName) {
        return generate(actionName, false);
    }

    public abstract PsiFile doGenerate(final String actionName);

    /**
     * Generate file.
     *
     * @param actionName String
     * @param openFile boolean
     *
     * @return PsiFile
     */
    public final PsiFile generate(final String actionName, final boolean openFile) {
        final PsiFile[] file = new PsiFile[1];
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project, () -> {
            file[0] = this.doGenerate(actionName);
        });

        if (file[0] != null && openFile) {
            navigateToCreatedFile.navigate(project, file[0]);
        }

        return file[0];
    }

    /**
     * Get file properties.
     *
     * @return Properties
     */
    protected Properties getAttributes() {
        final Properties attributes = new Properties();
        this.fillAttributes(attributes);

        return attributes;
    }

    /**
     * Fill attributes to be accessible from the file template.
     *
     * @param attributes Properties
     */
    protected abstract void fillAttributes(final Properties attributes);
}
