/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.UsedTypeInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class UsedNonExistentType extends UsedTypeInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass phpClass,
            final ClassReference reference
    ) {
        if (VersionStateManager.getInstance(project).isExists(phpClass.getFQN())) {
            return;
        }
        final String message = SupportedIssue.USED_NON_EXISTENT_TYPE.getMessage(
                phpClass.getFQN(),
                VersionStateManager.getInstance(project).getRemovedInVersion(phpClass.getFQN())
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.USED_NON_EXISTENT_TYPE
            );
        }
        problemsHolder.registerProblem(reference, message, ProblemHighlightType.ERROR);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.USED_NON_EXISTENT_TYPE.getLevel();
    }
}
