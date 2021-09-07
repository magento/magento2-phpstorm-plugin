/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.bundles.UctInspectionBundle;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ImportingDeprecatedType extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            private final UctInspectionBundle bundle = new UctInspectionBundle();
            private PhpClass phpClass;

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                phpClass = clazz;
            }

            @Override
            public void visitPhpUseList(final PhpUseList useList) {
                if (phpClass == null) {
                    return;
                }
                for (final PhpUse use : useList.getDeclarations()) {
                    if (VersionStateManager.getInstance().isDeprecated(use.getFQN())) {
                        final PhpReference phpReference = use.getTargetReference();
                        boolean isInterface = false;

                        if (phpReference != null) {
                            final PsiElement element = phpReference.resolve();

                            if (element instanceof PhpClass
                                    && ((PhpClass) element).isInterface()) {
                                isInterface = true;
                            }
                        }
                        String bundleKey = "customCode.warnings.deprecated.1132";

                        if (isInterface) {
                            bundleKey = "customCode.warnings.deprecated.1332";
                        }
                        final String message = bundle.message(
                                bundleKey,
                                use.getFQN()
                        );
                        problemsHolder.registerProblem(
                                use,
                                message,
                                ProblemHighlightType.LIKE_DEPRECATED
                        );
                    }
                }
            }
        };
    }
}
