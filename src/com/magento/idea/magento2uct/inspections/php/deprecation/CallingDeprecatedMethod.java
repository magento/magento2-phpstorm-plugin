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
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class CallingDeprecatedMethod extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpMethodReference(final MethodReference reference) {
                final Project project = reference.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled() || !settings.isIssueLevelSatisfiable(
                        SupportedIssue.CALLING_DEPRECATED_METHOD.getLevel())
                ) {
                    return;
                }
                final PsiElement resolvedElement = reference.resolve();

                if (!(resolvedElement instanceof Method)) {
                    return;
                }
                final String type = ((Method) resolvedElement).getFQN();

                if (VersionStateManager.getInstance(project).isDeprecated(type)) {
                    if (problemsHolder instanceof UctProblemsHolder) {
                        ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                SupportedIssue.CALLING_DEPRECATED_METHOD.getCode()
                        );
                    }
                    problemsHolder.registerProblem(
                            reference,
                            SupportedIssue.CALLING_DEPRECATED_METHOD.getMessage(
                                    type.replace(".", "::")
                            ),
                            ProblemHighlightType.LIKE_DEPRECATED
                    );
                }
            }
        };
    }
}
