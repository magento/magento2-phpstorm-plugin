/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.api;

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

public class OverriddenNonApiProperty extends OverriddenFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field property,
            final Field overriddenProperty,
            final PhpClass parentClass
    ) {
        if (VersionStateManager.getInstance(project).isApi(overriddenProperty.getFQN())) {
            return;
        }
        final String message = SupportedIssue.OVERRIDDEN_NON_API_PROPERTY.getMessage(
                overriddenProperty.getFQN().replace(".", "::")
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.OVERRIDDEN_NON_API_PROPERTY
            );
        }
        problemsHolder.registerProblem(property, message, ProblemHighlightType.WARNING);
    }

    @Override
    protected boolean isTypeValid(final Field field) {
        return !(field instanceof ClassConstImpl);
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.OVERRIDDEN_NON_API_PROPERTY.getLevel();
    }
}
