/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.api;

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

public class ExtendedNonApiClass extends ExtendInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass parentClass,
            final ExtendsList childExtends
    ) {
        if (VersionStateManager.getInstance(project).isApi(parentClass.getFQN())) {
            return;
        }
        final String message = SupportedIssue.EXTENDED_NON_API_CLASS.getMessage(
                parentClass.getFQN()
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.EXTENDED_NON_API_CLASS
            );
        }

        for (final ClassReference reference : childExtends.getReferenceElements()) {
            if (parentClass.getFQN().equals(reference.getFQN())) {
                problemsHolder.registerProblem(reference, message, ProblemHighlightType.WARNING);
            }
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.EXTENDED_NON_API_CLASS.getLevel();
    }
}
