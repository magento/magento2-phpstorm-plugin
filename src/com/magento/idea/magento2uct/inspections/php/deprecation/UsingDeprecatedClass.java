/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

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

public class UsingDeprecatedClass extends UsedTypeInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass phpClass,
            final ClassReference reference
    ) {
        if (phpClass.isInterface()
                || !VersionStateManager.getInstance(project).isDeprecated(phpClass.getFQN())) {
            return;
        }
        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.USING_DEPRECATED_CLASS
            );
        }
        final String deprecatedIn = VersionStateManager.getInstance(project)
                .getDeprecatedInVersion(phpClass.getFQN());

        problemsHolder.registerProblem(
                reference,
                SupportedIssue.USING_DEPRECATED_CLASS.getMessage(
                        phpClass.getFQN(),
                        deprecatedIn
                ),
                ProblemHighlightType.LIKE_DEPRECATED
        );
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.USING_DEPRECATED_CLASS.getLevel();
    }
}
