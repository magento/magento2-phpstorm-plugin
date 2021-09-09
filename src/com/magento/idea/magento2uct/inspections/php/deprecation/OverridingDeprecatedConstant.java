/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeAnalyserVisitor;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class OverridingDeprecatedConstant extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpTypeAnalyserVisitor() {

            @Override
            public void visitPhpField(final Field field) {
                super.visitPhpField(field);

                if (!(field instanceof ClassConstImpl)) {
                    return;
                }
                final ClassConstImpl constant = (ClassConstImpl) field;
                final PhpClass phpClass = field.getContainingClass();

                if (phpClass == null) {
                    return;
                }
                PhpClass parentClass = phpClass.getSuperClass();
                boolean isFound = false;

                while (parentClass != null && !isFound) {
                    for (final Field ownField : parentClass.getOwnFields()) {
                        if (ownField instanceof ClassConstImpl
                                && ownField.getName().equals(constant.getName())
                                && VersionStateManager.getInstance().isDeprecated(ownField.getFQN())
                        ) {
                            if (problemsHolder instanceof UctProblemsHolder) {
                                ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                        SupportedIssue.OVERRIDING_DEPRECATED_CONSTANT.getCode()
                                );
                            }
                            problemsHolder.registerProblem(
                                    constant,
                                    SupportedIssue.OVERRIDING_DEPRECATED_CONSTANT.getMessage(
                                            parentClass.getFQN()
                                                    .concat("::")
                                                    .concat(ownField.getName())
                                    ),
                                    ProblemHighlightType.LIKE_DEPRECATED
                            );
                            isFound = true;
                            break;
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }
        };
    }
}
