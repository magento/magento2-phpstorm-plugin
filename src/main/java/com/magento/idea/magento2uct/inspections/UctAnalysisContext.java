/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.UctVersionState;
import com.magento.idea.magento2uct.versioning.VersionStateManager;

/** Immutable options carried by a batch holder; editor inspections keep using project settings. */
public record UctAnalysisContext(IssueSeverityLevel minimumSeverity, UctVersionState versionState) {
    private static UctAnalysisContext context(final ProblemsHolder holder) {
        return holder instanceof UctProblemsHolder uctHolder ? uctHolder.getAnalysisContext() : null;
    }

    public static boolean isEnabled(final ProblemsHolder holder) {
        return context(holder) != null || UctSettingsService.getInstance(holder.getProject()).isEnabled();
    }

    public static boolean accepts(final ProblemsHolder holder, final IssueSeverityLevel severity) {
        final UctAnalysisContext context = context(holder);
        return context == null
                ? UctSettingsService.getInstance(holder.getProject()).isIssueLevelSatisfiable(severity)
                : severity.getLevel() <= context.minimumSeverity().getLevel();
    }

    public static UctVersionState versions(final ProblemsHolder holder) {
        final UctAnalysisContext context = context(holder);
        return context == null ? VersionStateManager.getInstance(holder.getProject()) : context.versionState();
    }
}
