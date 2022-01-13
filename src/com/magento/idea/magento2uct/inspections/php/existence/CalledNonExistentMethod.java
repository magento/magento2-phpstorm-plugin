/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.CallMethodInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class CalledNonExistentMethod extends CallMethodInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final MethodReference methodReference,
            final Method method
    ) {
        final String type = method.getFQN();

        if (VersionStateManager.getInstance(project).isExists(type)) {
            return;
        }
        final String message = SupportedIssue.CALLED_NON_EXISTENT_METHOD.getMessage(
                type,
                VersionStateManager.getInstance(project).getRemovedInVersion(type)
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.CALLED_NON_EXISTENT_METHOD
            );
        }
        problemsHolder.registerProblem(methodReference, message, ProblemHighlightType.ERROR);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.CALLED_NON_EXISTENT_METHOD.getLevel();
    }
}
