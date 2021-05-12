/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic.github;

import com.intellij.openapi.project.Project;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public final class GitHubNewIssueUrlBuilderUtil {

    private static final String NEW_BUG_ISSUE_BASE_URL = "https://github.com/magento/"
            + "magento2-phpstorm-plugin/issues/new"
            + "?assignees=&labels=bug&template=bug_report.md";
    private static final String URI_PARAMS_PART = "&title=%title&body=%body";
    private static final int MAX_URI_LENGTH = 8000;

    private GitHubNewIssueUrlBuilderUtil() {}

    /**
     * Build new issue url (template -> bug_report).
     *
     * @param title String
     * @param bugDescription String
     * @param stackTrace String
     * @param project Project
     *
     * @return String
     */
    public static String buildNewBugIssueUrl(
            final @NotNull String title,
            final @NotNull String bugDescription,
            final @NotNull String stackTrace,
            final @NotNull Project project
    ) {
        final String bugReportBody = GitHubNewIssueBodyBuilderUtil.buildNewBugReportBody(
                project,
                bugDescription,
                stackTrace,
                getAllowedBodyLength(title)
        );

        return formatNewBugIssueUrl(title, bugReportBody);
    }

    /**
     * Format URL with encoded url parameters.
     *
     * @param title String
     * @param body String
     *
     * @return String
     */
    private static String formatNewBugIssueUrl(
            final @NotNull String title,
            final @NotNull String body
    ) {
        final String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        final String encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8);

        final String paramsPart = URI_PARAMS_PART
                .replace("%title", encodedTitle)
                .replace("%body", encodedBody);

        return NEW_BUG_ISSUE_BASE_URL.concat(paramsPart);
    }

    /**
     * Calculate max allowed body length.
     *
     * @param title String
     *
     * @return int
     */
    private static int getAllowedBodyLength(final @NotNull String title) {
        return MAX_URI_LENGTH - formatNewBugIssueUrl(title, "").length();
    }
}
