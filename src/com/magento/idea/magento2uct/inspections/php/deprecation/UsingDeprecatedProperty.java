/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class UsingDeprecatedProperty extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpFieldReference(final FieldReference fieldReference) {
                final PsiElement element = fieldReference.resolve();
                final PsiFile file = fieldReference.getContainingFile();

                if (!(element instanceof Field) || element.getContainingFile() == file) {
                    return;
                }
                final Field field = (Field) element;

                if (VersionStateManager.getInstance(fieldReference.getProject())
                        .isDeprecated(field.getFQN())) {
                    if (problemsHolder instanceof UctProblemsHolder) {
                        ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                SupportedIssue.USING_DEPRECATED_PROPERTY.getCode()
                        );
                    }
                    problemsHolder.registerProblem(
                            fieldReference,
                            SupportedIssue.USING_DEPRECATED_PROPERTY.getMessage(
                                    field.getFQN().replace(".", "::")
                            ),
                            ProblemHighlightType.LIKE_DEPRECATED
                    );
                } else {
                    final PhpClass containingClass = field.getContainingClass();

                    if (containingClass == null) {
                        return;
                    }
                    if (VersionStateManager.getInstance(fieldReference.getProject())
                            .isDeprecated(containingClass.getFQN())) {
                        if (problemsHolder instanceof UctProblemsHolder) {
                            ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                    SupportedIssue.USING_DEPRECATED_PROPERTY.getCode()
                            );
                        }
                        problemsHolder.registerProblem(
                                fieldReference,
                                SupportedIssue.USING_DEPRECATED_PROPERTY.getMessage(
                                        field.getFQN().replace(".", "::")
                                ),
                                ProblemHighlightType.LIKE_DEPRECATED
                        );
                    }
                }
            }
        };
    }
}
