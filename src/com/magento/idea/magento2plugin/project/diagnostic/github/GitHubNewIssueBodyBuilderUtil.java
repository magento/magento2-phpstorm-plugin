/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic.github;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
     * @param maxAllowedBodyLength int
     *
     * @return String
     */
    public static String buildNewBugReportBody(
            final @NotNull Project project,
            final @NotNull String bugDescription,
            final @NotNull String stackTrace,
            final int maxAllowedBodyLength
    ) {
        final int maxAllowedStackTraceLength = getMaxAllowedStackTraceLength(
                project,
                bugDescription,
                maxAllowedBodyLength
        );

        if (encode(stackTrace).length() <= maxAllowedStackTraceLength) {
            return buildTemplate(project, bugDescription, stackTrace);
        }

        final String cutStackTrace = encode(stackTrace).substring(
                0,
                maxAllowedStackTraceLength - 1
        );

        return buildTemplate(project, bugDescription, decode(cutStackTrace));
    }

    /**
     * Build bug report body template.
     *
     * @param project Project
     * @param bugDescription String
     * @param stackTrace String
     *
     * @return String
     */
    private static String buildTemplate(
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

    /**
     * Get max allowed stacktrace length.
     *
     * @param project Project
     * @param bugDescription String
     * @param maxAllowedBodyLength String
     *
     * @return int
     */
    private static int getMaxAllowedStackTraceLength(
            final @NotNull Project project,
            final @NotNull String bugDescription,
            final int maxAllowedBodyLength
    ) {
        final String builtTemplateWithoutStackTrace = buildTemplate(project, bugDescription, "");

        return maxAllowedBodyLength - encode(builtTemplateWithoutStackTrace).length();
    }

    /**
     * Encode string to be used in URI.
     *
     * @param value String
     *
     * @return String
     */
    private static String encode(final @NotNull String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Decode string that was encoded to be used in URI.
     *
     * @param value String
     *
     * @return String
     */
    private static String decode(final @NotNull String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
