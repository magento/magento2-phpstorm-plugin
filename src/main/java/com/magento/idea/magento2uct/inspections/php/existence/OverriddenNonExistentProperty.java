/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.existence;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.OverriddenFieldInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class OverriddenNonExistentProperty extends OverriddenFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field property,
            final Field overriddenProperty,
            final PhpClass parentClass
    ) {
        if (VersionStateManager.getInstance(project).isExists(overriddenProperty.getFQN())) {
            return;
        }
        final String messageArg = parentClass
                .getFQN()
                .concat("::")
                .concat(overriddenProperty.getName());

        final String message = SupportedIssue.OVERRIDDEN_NON_EXISTENT_PROPERTY.getMessage(
                messageArg,
                VersionStateManager.getInstance(project).getRemovedInVersion(
                        overriddenProperty.getFQN()
                )
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.OVERRIDDEN_NON_EXISTENT_PROPERTY
            );
        }
        problemsHolder.registerProblem(property, message, ProblemHighlightType.ERROR);
    }

    @Override
    protected boolean isTypeValid(final Field field) {
        return !(field instanceof ClassConstImpl);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.OVERRIDDEN_NON_EXISTENT_PROPERTY.getLevel();
    }
}
