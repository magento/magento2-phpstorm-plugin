/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.util.Consumer;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.project.diagnostic.github.GitHubNewIssueUrlBuilderUtil;
import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultErrorReportSubmitter extends ErrorReportSubmitter {

    private static final String DEFAULT_ISSUE_TITLE = "Bug Report %date";
    private static final String DEFAULT_ISSUE_DESCRIPTION
            = "A clear and concise description of what the bug is.";
    private final CommonBundle commonBundle;

    /**
     * Default error report submitter.
     */
    public DefaultErrorReportSubmitter() {
        super();
        commonBundle = new CommonBundle();
    }

    /**
     * Open GitHub link with creation of the new bug report issue.
     *
     * @param events IdeaLoggingEvent[]
     * @param additionalInfo String
     * @param parentComponent Component
     * @param consumer Consumer
     *
     * @return boolean
     */
    @Override
    public boolean submit(
            final @NotNull IdeaLoggingEvent[] events,
            final @Nullable String additionalInfo,
            final @NotNull Component parentComponent,
            final @NotNull Consumer<? super SubmittedReportInfo> consumer
    ) {
        final DataContext context = DataManager.getInstance().getDataContext(parentComponent);
        final Project project = CommonDataKeys.PROJECT.getData(context);

        if (project == null) {
            return false;
        }
        final StringBuilder stackTrace = new StringBuilder();

        for (final IdeaLoggingEvent event : events) {
            stackTrace.append(event.getThrowableText()).append("\r\n");
        }

        BrowserUtil.browse(
                GitHubNewIssueUrlBuilderUtil.buildNewBugIssueUrl(
                        getDefaultIssueTitle(),
                        additionalInfo == null ? DEFAULT_ISSUE_DESCRIPTION : additionalInfo,
                        stackTrace.toString(),
                        project
                )
        );

        ApplicationManager.getApplication().invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    parentComponent,
                    commonBundle.message("common.diagnostic.reportSubmittedMessage"),
                    commonBundle.message("common.diagnostic.reportSubmittedTitle"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            consumer.consume(
                    new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
            );
        });

        return true;
    }

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return commonBundle.message("common.diagnostic.reportButtonText");
    }

    /**
     * Get default bug issue title.
     *
     * @return String
     */
    private String getDefaultIssueTitle() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        return DEFAULT_ISSUE_TITLE.replace("%date", formatter.format(LocalDateTime.now()));
    }
}
