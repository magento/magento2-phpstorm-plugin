/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic.github;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public final class GitHubNewIssueUrlBuilderUtil {

    private static final String NEW_BUG_ISSUE_BASE_URL = "https://github.com/magento/"
            + "magento2-phpstorm-plugin/issues/new"
            + "?assignees=&labels=bug&template=bug_report.md";

    private GitHubNewIssueUrlBuilderUtil() {}

    /**
     * Build new issue url (template -> bug_report).
     *
     * @param title String
     * @param body String
     *
     * @return String
     */
    public static String buildNewBugIssueUrl(
            final @NotNull String title,
            final @NotNull String body
    ) {
        final String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        final String encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8);

        return NEW_BUG_ISSUE_BASE_URL
                .concat("&title=" + encodedTitle)
                .concat("&body=" + encodedBody);
    }
}
