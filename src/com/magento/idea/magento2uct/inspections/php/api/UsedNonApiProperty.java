/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.api;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.UsedFieldInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class UsedNonApiProperty extends UsedFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field property,
            final FieldReference propertyReference
    ) {
        if (VersionStateManager.getInstance(project).isApi(property.getFQN())) {
            return;
        }
        final String message = SupportedIssue.USED_NON_API_PROPERTY.getMessage(
                property.getFQN().replace(".", "::")
        );

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(SupportedIssue.USED_NON_API_PROPERTY);
        }
        problemsHolder.registerProblem(propertyReference, message, ProblemHighlightType.WARNING);
    }

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassConstImpl constant,
            final ClassConstantReference constantReference
    ) {
        // We do not need to check constant in the field inspection.
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.USED_NON_API_PROPERTY.getLevel();
    }
}
