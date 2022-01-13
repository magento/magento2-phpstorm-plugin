/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.ImportInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ImportingDeprecatedInterface extends ImportInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpUse use,
            final boolean isInterface
    ) {
        if (VersionStateManager.getInstance(project).isDeprecated(use.getFQN())) {
            if (!isInterface) {
                return;
            }
            if (problemsHolder instanceof UctProblemsHolder) {
                ((UctProblemsHolder) problemsHolder).setIssue(
                        SupportedIssue.IMPORTING_DEPRECATED_INTERFACE
                );
            }
            final String deprecatedIn = VersionStateManager.getInstance(project)
                    .getDeprecatedInVersion(use.getFQN());

            problemsHolder.registerProblem(
                    use,
                    SupportedIssue.IMPORTING_DEPRECATED_INTERFACE.getMessage(
                            use.getFQN(),
                            deprecatedIn
                    ),
                    ProblemHighlightType.LIKE_DEPRECATED
            );
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.IMPORTING_DEPRECATED_INTERFACE.getLevel();
    }
}
