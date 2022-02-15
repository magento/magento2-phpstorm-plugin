/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.InheritedInterfaceInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class InheritedDeprecatedInterface extends InheritedInterfaceInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassReference reference,
            final String interfaceFqn
    ) {
        if (VersionStateManager.getInstance(project).isDeprecated(interfaceFqn)) {
            if (problemsHolder instanceof UctProblemsHolder) {
                ((UctProblemsHolder) problemsHolder).setIssue(
                        SupportedIssue.INHERITED_DEPRECATED_INTERFACE
                );
            }
            final String deprecatedIn = VersionStateManager.getInstance(project)
                    .getDeprecatedInVersion(interfaceFqn);

            problemsHolder.registerProblem(
                    reference,
                    SupportedIssue.INHERITED_DEPRECATED_INTERFACE.getMessage(
                            interfaceFqn,
                            deprecatedIn
                    ),
                    ProblemHighlightType.LIKE_DEPRECATED
            );
        }
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.INHERITED_DEPRECATED_INTERFACE.getLevel();
    }
}
