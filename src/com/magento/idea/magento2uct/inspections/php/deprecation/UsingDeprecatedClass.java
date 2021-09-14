/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import org.jetbrains.annotations.NotNull;

public class UsingDeprecatedClass extends UsingDeprecatedType {

    @Override
    protected void registerProblem(
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final String fieldClass,
            final boolean isInterface
    ) {
        if (isInterface) {
            return;
        }
        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                    SupportedIssue.USING_DEPRECATED_CLASS.getCode()
            );
        }
        problemsHolder.registerProblem(
                field,
                SupportedIssue.USING_DEPRECATED_CLASS.getMessage(fieldClass),
                ProblemHighlightType.LIKE_DEPRECATED
        );
    }

    @Override
    protected void registerReferenceProblem(
            final @NotNull ProblemsHolder problemsHolder,
            final ClassReference reference,
            final String deprecatedType,
            final boolean isInterface
    ) {
        if (isInterface) {
            return;
        }
        if (problemsHolder instanceof UctProblemsHolder) {
            ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                    SupportedIssue.USING_DEPRECATED_CLASS.getCode()
            );
        }
        problemsHolder.registerProblem(
                reference,
                SupportedIssue.USING_DEPRECATED_CLASS.getMessage(deprecatedType),
                ProblemHighlightType.LIKE_DEPRECATED
        );
    }

    @Override
    protected IssueSeverityLevel getSeverityLevel() {
        return SupportedIssue.USING_DEPRECATED_CLASS.getLevel();
    }
}
