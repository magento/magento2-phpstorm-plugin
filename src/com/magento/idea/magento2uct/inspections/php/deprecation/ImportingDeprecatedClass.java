/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import org.jetbrains.annotations.NotNull;

public class ImportingDeprecatedClass extends ImportingDeprecatedType {

    @Override
    protected void registerProblem(
            final @NotNull ProblemsHolder problemsHolder,
            final PhpUse use,
            final boolean isInterface
    ) {
        if (!isInterface) {
            if (problemsHolder instanceof UctProblemsHolder) {
                ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                        SupportedIssue.IMPORTING_DEPRECATED_CLASS.getCode()
                );
            }
            problemsHolder.registerProblem(
                    use,
                    SupportedIssue.IMPORTING_DEPRECATED_CLASS.getMessage(use.getFQN()),
                    ProblemHighlightType.LIKE_DEPRECATED
            );
        }
    }
}
