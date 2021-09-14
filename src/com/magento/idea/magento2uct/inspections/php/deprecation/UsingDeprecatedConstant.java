/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class UsingDeprecatedConstant extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpClassConstantReference(
                    final ClassConstantReference constantReference
            ) {
                final Project project = constantReference.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled() || !settings.isIssueLevelSatisfiable(
                        SupportedIssue.USING_DEPRECATED_CONSTANT.getLevel())
                ) {
                    return;
                }
                final PsiElement element = constantReference.resolve();

                if (!(element instanceof ClassConstImpl)) {
                    return;
                }
                final String constantClass = ((ClassConstImpl) element).getFQN();

                if (!VersionStateManager.getInstance(project).isDeprecated(constantClass)) {
                    return;
                }
                final PhpClass containingClass = ((ClassConstImpl) element).getContainingClass();

                if (containingClass == null) {
                    return;
                }
                if (problemsHolder instanceof UctProblemsHolder) {
                    ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                            SupportedIssue.USING_DEPRECATED_CONSTANT.getCode()
                    );
                }
                problemsHolder.registerProblem(
                        constantReference,
                        SupportedIssue.USING_DEPRECATED_CONSTANT.getMessage(
                                containingClass.getFQN()
                                        .concat("::")
                                        .concat(((ClassConstImpl) element).getName())
                        ),
                        ProblemHighlightType.LIKE_DEPRECATED
                );
            }
        };
    }
}
