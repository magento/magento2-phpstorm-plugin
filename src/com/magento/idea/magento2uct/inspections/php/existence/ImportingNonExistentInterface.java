/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

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

public class ImportingNonExistentInterface extends ImportInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpUse use,
            final boolean isInterface
    ) {
        if (!isInterface || VersionStateManager.getInstance(project).isExists(use.getFQN())) {
            return;
        }
        final String message = SupportedIssue.IMPORTED_NON_EXISTENT_INTERFACE.getMessage(
                use.getFQN(),
                VersionStateManager.getInstance(project).getRemovedInVersion(use.getFQN())
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.IMPORTED_NON_EXISTENT_INTERFACE
            );
        }
        problemsHolder.registerProblem(
                use,
                message,
                ProblemHighlightType.ERROR
        );
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.IMPORTED_NON_EXISTENT_INTERFACE.getLevel();
    }
}
