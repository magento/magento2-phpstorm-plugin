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
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import org.jetbrains.annotations.NotNull;

public class ExtendingDeprecatedClass extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                PhpClass parentClass = clazz.getSuperClass();

                if (parentClass == null) {
                    return;
                }
                final ExtendsList list = clazz.getExtendsList();
                final String parentClassFqn = parentClass.getFQN();

                while (parentClass != null) {
                    if (VersionStateManager.getInstance().isDeprecated(parentClass.getFQN())) {
                        for (final ClassReference classReference : list.getReferenceElements()) {
                            if (parentClassFqn.equals(classReference.getFQN())) {
                                if (problemsHolder instanceof UctProblemsHolder) {
                                    ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                            SupportedIssue.EXTENDING_DEPRECATED_CLASS.getCode()
                                    );
                                }
                                problemsHolder.registerProblem(
                                        classReference,
                                        SupportedIssue.EXTENDING_DEPRECATED_CLASS.getMessage(
                                                parentClass.getFQN()
                                        ),
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
