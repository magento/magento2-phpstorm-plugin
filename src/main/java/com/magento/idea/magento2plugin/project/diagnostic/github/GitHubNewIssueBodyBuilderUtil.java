/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic.github;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.jar.Manifest;
import org.jetbrains.annotations.NotNull;

public final class GitHubNewIssueBodyBuilderUtil {

    private static final String BUR_REPORT_TEMPLATE = "GitHub New Bug Issue Body Template";
    private static final String CLASS_FILE_NAME = "GitHubNewIssueBodyBuilderUtil.class";
    private static final String VERSION_MANIFEST_ATTRIBUTE = "Version";

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
        int maxAllowedStackTraceLength = getMaxAllowedStackTraceLength(
                project,
                bugDescription,
                maxAllowedBodyLength
        );

        if (encode(stackTrace).length() <= maxAllowedStackTraceLength) {
            return buildTemplate(project, bugDescription, stackTrace);
        }
        boolean isFound = false;
        int step = 1;
        String encodedCutStackTrace = "";

        while (!isFound) {
            if (stackTrace.length() < maxAllowedStackTraceLength) {
                maxAllowedStackTraceLength = stackTrace.length();
            }
            final String cutStackTrace = stackTrace.substring(0, maxAllowedStackTraceLength - step);
            encodedCutStackTrace = encode(cutStackTrace);

            if (encodedCutStackTrace.length() <= maxAllowedStackTraceLength) {
                isFound = true;
            } else {
                step += 10;
            }
        }

        return buildTemplate(project, bugDescription, decode(encodedCutStackTrace));
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
        properties.setProperty("OS_VERSION", getOsVersion());
        properties.setProperty("INTELLIJ_VERSION", getIntellijVersion());
        properties.setProperty("PLUGIN_VERSION", getPluginVersion());

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

    /**
     * Get OS version.
     *
     * @return String
     */
    private static String getOsVersion() {
        return SystemInfo.OS_NAME + " " + SystemInfo.OS_VERSION;
    }

    /**
     * Get Intellij Idea version.
     *
     * @return String
     */
    private static String getIntellijVersion() {
        return ApplicationInfo.getInstance().getFullVersion();
    }

    /**
     * Get plugin version.
     *
     * @return String
     */
    private static String getPluginVersion() {
        final java.net.URL classUrl = GitHubNewIssueBodyBuilderUtil.class.getResource(
                CLASS_FILE_NAME
        );

        if (classUrl == null || !"jar".equals(classUrl.getProtocol())) {
            return "";
        }

        try {
            final JarURLConnection connection = (JarURLConnection) classUrl.openConnection();
            final Manifest manifest = connection.getManifest();

            if (manifest == null) {
                return "";
            }

            final String version = manifest.getMainAttributes().getValue(
                    VERSION_MANIFEST_ATTRIBUTE
            );

            return version == null ? "" : version;
        } catch (IOException exception) {
            return "";
        }
    }
}
