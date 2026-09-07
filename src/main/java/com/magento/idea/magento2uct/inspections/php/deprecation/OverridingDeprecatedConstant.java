/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

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

public class OverridingDeprecatedConstant extends OverriddenFieldInspection {

    @Override
    protected void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final Field overriddenField,
            final PhpClass parentClass
    ) {
        if (!VersionStateManager.getInstance(project).isDeprecated(overriddenField.getFQN())) {
            return;
        }

        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setIssue(
                    SupportedIssue.OVERRIDING_DEPRECATED_CONSTANT
            );
        }
        final String deprecatedIn = VersionStateManager.getInstance(project)
                .getDeprecatedInVersion(overriddenField.getFQN());

        problemsHolder.registerProblem(
                field,
                SupportedIssue.OVERRIDING_DEPRECATED_CONSTANT.getMessage(
                        parentClass.getFQN()
                                .concat("::")
                                .concat(overriddenField.getName()),
                        deprecatedIn
                ),
                ProblemHighlightType.LIKE_DEPRECATED
        );
    }

    @Override
    protected boolean isTypeValid(final Field field) {
        return field instanceof ClassConstImpl;
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.OVERRIDING_DEPRECATED_CONSTANT.getLevel();
    }
}
