/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.magento.idea.magento2uct.analysis.UctReleasePreparation;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.UctVersionState;

/** Immutable options carried by a batch holder; editor inspections keep using project settings. */
public record UctAnalysisContext(IssueSeverityLevel minimumSeverity, UctVersionState versionState) {
    private static UctAnalysisContext context(final ProblemsHolder holder) {
        return holder instanceof UctProblemsHolder uctHolder ? uctHolder.getAnalysisContext() : null;
    }

    public static boolean isEnabled(final ProblemsHolder holder) {
        if (context(holder) != null) {
            return true;
        }
        final UctSettingsService settings = UctSettingsService.getInstance(holder.getProject());
        return settings.isEnabled()
                && holder.getProject().getService(UctReleasePreparation.class).hasEditorData(
                        settings.getConfiguredCurrentVersion(),
                        settings.getConfiguredTargetVersion(),
                        Boolean.TRUE.equals(settings.shouldIgnoreCurrentVersion())
                );
    }

    public static boolean accepts(final ProblemsHolder holder, final IssueSeverityLevel severity) {
        final UctAnalysisContext context = context(holder);
        return context == null
                ? UctSettingsService.getInstance(holder.getProject()).isIssueLevelSatisfiable(severity)
                : severity.getLevel() <= context.minimumSeverity().getLevel();
    }

    public static UctVersionState versions(final ProblemsHolder holder) {
        final UctAnalysisContext context = context(holder);
        if (context != null) {
            return context.versionState();
        }
        final UctSettingsService settings = UctSettingsService.getInstance(holder.getProject());
        return holder.getProject().getService(UctReleasePreparation.class).editorState(
                settings.getConfiguredCurrentVersion(),
                settings.getConfiguredTargetVersion(),
                Boolean.TRUE.equals(settings.shouldIgnoreCurrentVersion())
        );
    }
}
