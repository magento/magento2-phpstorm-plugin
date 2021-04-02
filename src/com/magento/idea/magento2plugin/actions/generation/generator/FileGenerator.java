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
