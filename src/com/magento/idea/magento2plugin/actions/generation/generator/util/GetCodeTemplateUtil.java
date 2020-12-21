/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.Properties;

public final class GetCodeTemplateUtil {
    private final Project project;

    public GetCodeTemplateUtil(final Project project) {
        this.project = project;
    }

    /**
     * Returns code template string.
     *
     * @param templateName String
     * @param properties Properties
     * @return String
     *
     * @throws IOException template not found.
     */
    public String execute(
            final String templateName,
            final Properties properties
    ) throws IOException {
        final FileTemplate fileTemplate = FileTemplateManager.getInstance(project)
                .getCodeTemplate(templateName);
        return fileTemplate.getText(properties);
    }
}
