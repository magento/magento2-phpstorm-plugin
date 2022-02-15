/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

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

public class CallingDeprecatedMethod extends CallMethodInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final MethodReference methodReference,
            final Method method
    ) {
        final String type = method.getFQN();

        if (VersionStateManager.getInstance(project).isDeprecated(type)) {
            if (problemsHolder instanceof UctProblemsHolder) {
                ((UctProblemsHolder) problemsHolder).setIssue(
                        SupportedIssue.CALLING_DEPRECATED_METHOD
                );
            }
            final String deprecatedIn = VersionStateManager.getInstance(project)
                    .getDeprecatedInVersion(type);

            problemsHolder.registerProblem(
                    methodReference,
                    SupportedIssue.CALLING_DEPRECATED_METHOD.getMessage(
                            type.replace(".", "::"),
                            deprecatedIn
                    ),
                    ProblemHighlightType.LIKE_DEPRECATED
            );
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.CALLING_DEPRECATED_METHOD.getLevel();
    }
}
