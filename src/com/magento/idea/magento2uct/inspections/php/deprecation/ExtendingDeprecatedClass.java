/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

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

public class ExtendingDeprecatedClass extends ExtendInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass parentClass,
            final ExtendsList childExtends
    ) {
        final String parentFqn = parentClass.getFQN();

        if (!VersionStateManager.getInstance(project).isDeprecated(parentFqn)) {
            return;
        }
        final String deprecatedIn = VersionStateManager.getInstance(project)
                .getDeprecatedInVersion(parentFqn);

        for (final ClassReference classReference : childExtends.getReferenceElements()) {
            if (parentFqn.equals(classReference.getFQN())) {
                if (problemsHolder instanceof UctProblemsHolder) {
                    ((UctProblemsHolder) problemsHolder).setIssue(
                            SupportedIssue.EXTENDING_DEPRECATED_CLASS
                    );
                }
                problemsHolder.registerProblem(
                        classReference,
                        SupportedIssue.EXTENDING_DEPRECATED_CLASS.getMessage(
                                parentClass.getFQN(),
                                deprecatedIn
                        ),
                        ProblemHighlightType.LIKE_DEPRECATED
                );
            }
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.EXTENDING_DEPRECATED_CLASS.getLevel();
    }
}
