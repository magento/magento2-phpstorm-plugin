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

public class UsingDeprecatedProperty extends UsedFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final FieldReference fieldReference
    ) {
        if (VersionStateManager.getInstance(project).isDeprecated(field.getFQN())) {
            if (problemsHolder instanceof UctProblemsHolder) {
                ((UctProblemsHolder) problemsHolder).setIssue(
                        SupportedIssue.USING_DEPRECATED_PROPERTY
                );
            }
            final String deprecatedIn = VersionStateManager.getInstance(project)
                    .getDeprecatedInVersion(field.getFQN());

            problemsHolder.registerProblem(
                    fieldReference,
                    SupportedIssue.USING_DEPRECATED_PROPERTY.getMessage(
                            field.getFQN().replace(".", "::"),
                            deprecatedIn
                    ),
                    ProblemHighlightType.LIKE_DEPRECATED
            );
        } else {
            final PhpClass containingClass = field.getContainingClass();

            if (containingClass == null) {
                return;
            }
            if (VersionStateManager.getInstance(project).isDeprecated(containingClass.getFQN())) {
                if (problemsHolder instanceof UctProblemsHolder) {
                    ((UctProblemsHolder) problemsHolder).setIssue(
                            SupportedIssue.USING_DEPRECATED_PROPERTY
                    );
                }
                final String deprecatedIn = VersionStateManager.getInstance(project)
                        .getDeprecatedInVersion(containingClass.getFQN());

                problemsHolder.registerProblem(
                        fieldReference,
                        SupportedIssue.USING_DEPRECATED_PROPERTY.getMessage(
                                field.getFQN().replace(".", "::"),
                                deprecatedIn
                        ),
                        ProblemHighlightType.LIKE_DEPRECATED
                );
            }
        }
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
        return SupportedIssue.USING_DEPRECATED_PROPERTY.getLevel();
    }
}
