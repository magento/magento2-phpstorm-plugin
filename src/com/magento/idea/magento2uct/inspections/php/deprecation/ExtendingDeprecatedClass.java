/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.ExtendsList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.bundles.UctInspectionBundle;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ExtendingDeprecatedClass extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            private final UctInspectionBundle bundle = new UctInspectionBundle();

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                PhpClass parentClass = clazz.getSuperClass();

                if (parentClass == null) {
                    return;
                }
                final ExtendsList extendsList = clazz.getExtendsList();
                final String parentClassFqn = parentClass.getFQN();

                while (parentClass != null) {
                    if (VersionStateManager.getInstance().isDeprecated(parentClass.getFQN())) {
                        final String message = bundle.message(
                                "customCode.warnings.deprecated.1131",
                                parentClass.getFQN()
                        );

                        for (final ClassReference classReference
                                : extendsList.getReferenceElements()) {
                            if (parentClassFqn.equals(classReference.getFQN())) {
                                problemsHolder.registerProblem(
                                        classReference,
                                        message,
                                        ProblemHighlightType.LIKE_DEPRECATED
                                );
                            }
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }
        };
    }
}
