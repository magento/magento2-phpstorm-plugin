/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.ImplementInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ImplementedNonExistentInterface extends ImplementInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassReference reference,
            final String interfaceFqn
    ) {
        if (VersionStateManager.getInstance(project).isExists(interfaceFqn)) {
            return;
        }
        final String message = SupportedIssue.IMPLEMENTED_NON_EXISTENT_INTERFACE.getMessage(
                interfaceFqn,
                VersionStateManager.getInstance(project).getRemovedInVersion(interfaceFqn)
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.IMPLEMENTED_NON_EXISTENT_INTERFACE
            );
        }
        problemsHolder.registerProblem(reference, message, ProblemHighlightType.ERROR);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.IMPLEMENTED_NON_EXISTENT_INTERFACE.getLevel();
    }
}
