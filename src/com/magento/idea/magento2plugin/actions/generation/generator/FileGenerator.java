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
    private final Project project;
    private final NavigateToCreatedFile navigateToCreatedFile;

    public FileGenerator(final Project project) {
        this.project = project;
        this.navigateToCreatedFile = NavigateToCreatedFile.getInstance();
    }

    public abstract PsiFile generate(final String actionName);

    /**
     * Generate file.
     *
     * @param actionName String
     * @param openFile boolean
     *
     * @return PsiFile
     */
    public PsiFile generate(final String actionName, final boolean openFile) {
        final PsiFile file = this.generate(actionName);

        if (file != null && openFile) {
            navigateToCreatedFile.navigate(project, file);
        }
        return file;
    }

    protected Properties getAttributes() {
        final Properties attributes = new Properties();
        this.fillAttributes(attributes);
        return attributes;
    }

    protected abstract void fillAttributes(Properties attributes);
}
