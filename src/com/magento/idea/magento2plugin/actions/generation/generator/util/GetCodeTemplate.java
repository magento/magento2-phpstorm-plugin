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

public class GetCodeTemplate {
    private static GetCodeTemplate INSTANCE = null;
    private Project project;

    GetCodeTemplate(Project project) {
        this.project = project;
    }

    public static GetCodeTemplate getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new GetCodeTemplate(project);
        }

        return INSTANCE;
    }

    public String execute(String templateName, Properties properties) throws IOException {
        FileTemplate fileTemplate = FileTemplateManager.getInstance(project).getCodeTemplate(templateName);
        return fileTemplate.getText(properties);
    }
}
