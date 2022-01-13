/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.UsedFieldInspection;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class UsingDeprecatedConstant extends UsedFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final FieldReference fieldReference
    ) {
        // We do not need to check field in the constant inspection.
    }

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassConstImpl constant,
            final ClassConstantReference constantReference
    ) {
        final String constantFqn = constant.getFQN();

        if (!VersionStateManager.getInstance(project).isDeprecated(constantFqn)) {
            return;
        }
        final PhpClass containingClass = constant.getContainingClass();

        if (containingClass == null) {
            return;
        }

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.USING_DEPRECATED_CONSTANT
            );
        }
        final String deprecatedIn = VersionStateManager.getInstance(project)
                .getDeprecatedInVersion(constantFqn);

        problemsHolder.registerProblem(
                constantReference,
                SupportedIssue.USING_DEPRECATED_CONSTANT.getMessage(
                        containingClass.getFQN().concat("::").concat(constant.getName()),
                        deprecatedIn
                ),
                ProblemHighlightType.LIKE_DEPRECATED
        );
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.USING_DEPRECATED_CONSTANT.getLevel();
    }
}
