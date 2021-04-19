/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic.github;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public final class GitHubNewIssueBodyBuilderUtil {

    private static final String BUR_REPORT_TEMPLATE = "GitHub New Bug Issue Body Template";

    private GitHubNewIssueBodyBuilderUtil() {}

    /**
     * Build BUG report body.
     *
     * @param project Project
     * @param bugDescription String
     * @param stackTrace String
     *
     * @return String
     */
    public static String buildNewBugReportBody(
            final @NotNull Project project,
            final @NotNull String bugDescription,
            final @NotNull String stackTrace
    ) {
        final FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        final FileTemplate errorReportTemplate =
                templateManager.getCodeTemplate(BUR_REPORT_TEMPLATE);

        final Properties properties = new Properties();
        properties.setProperty("BUG_DESCRIPTION", bugDescription);
        properties.setProperty("STACK_TRACE", stackTrace);

        try {
            return errorReportTemplate.getText(properties);
        } catch (IOException exception) {
            return "";
        }
    }
}
