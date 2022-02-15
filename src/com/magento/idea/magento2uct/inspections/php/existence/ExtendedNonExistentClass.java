/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ExtendsList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.ExtendInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ExtendedNonExistentClass extends ExtendInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass parentClass,
            final ExtendsList childExtends
    ) {
        final String parentFqn = parentClass.getFQN();

        if (VersionStateManager.getInstance(project).isExists(parentFqn)) {
            return;
        }
        final String message = SupportedIssue.EXTENDED_NON_EXISTENT_CLASS.getMessage(
                parentFqn,
                VersionStateManager.getInstance(project).getRemovedInVersion(parentFqn)
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.EXTENDED_NON_EXISTENT_CLASS
            );
        }

        for (final ClassReference reference : childExtends.getReferenceElements()) {
            if (parentFqn.equals(reference.getFQN())) {
                problemsHolder.registerProblem(reference, message, ProblemHighlightType.ERROR);
            }
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.EXTENDED_NON_EXISTENT_CLASS.getLevel();
    }
}
