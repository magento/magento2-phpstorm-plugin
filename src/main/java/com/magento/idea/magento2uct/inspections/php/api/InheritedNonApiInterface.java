/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.api;

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

public class InheritedNonApiInterface extends InheritedInterfaceInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassReference reference,
            final String fqn
    ) {
        if (VersionStateManager.getInstance(project).isApi(fqn)) {
            return;
        }
        final String message = SupportedIssue.INHERITED_NON_API_INTERFACE.getMessage(fqn);

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.INHERITED_NON_API_INTERFACE
            );
        }
        problemsHolder.registerProblem(reference, message, ProblemHighlightType.WARNING);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.INHERITED_NON_API_INTERFACE.getLevel();
    }
}
