/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php.deprecation;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.packages.SupportedIssue;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ImplementedDeprecatedInterface extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                final Project project = clazz.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled() || !settings.isIssueLevelSatisfiable(
                        SupportedIssue.IMPLEMENTED_DEPRECATED_INTERFACE.getLevel())
                ) {
                    return;
                }

                if (clazz.isInterface()) {
                    return;
                }
                for (final ClassReference ref : clazz.getImplementsList().getReferenceElements()) {
                    final String interfaceFqn = ref.getFQN();
                    final PsiElement interfaceClass = ref.resolve();

                    if (interfaceFqn == null || !(interfaceClass instanceof PhpClass)) {
                        continue;
                    }
                    final boolean isDeprecated = VersionStateManager
                            .getInstance(project).isDeprecated(interfaceFqn);
                    Pair<Boolean, String> checkResult = null;

                    if (isDeprecated || (checkResult = InheritedDeprecatedInterface
                            .hasDeprecatedParent((PhpClass) interfaceClass)).getFirst()) {
                        if (problemsHolder instanceof UctProblemsHolder) {
                            ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                    SupportedIssue.IMPLEMENTED_DEPRECATED_INTERFACE.getCode()
                            );
                        }
                        problemsHolder.registerProblem(
                                ref,
                                SupportedIssue.IMPLEMENTED_DEPRECATED_INTERFACE.getMessage(
                                        checkResult == null
                                                ? interfaceFqn
                                                : checkResult.getSecond()
                                ),
                                ProblemHighlightType.LIKE_DEPRECATED
                        );
                    }
                }

                PhpClass parentClass = clazz.getSuperClass();

                while (parentClass != null) {
                    final Pair<Boolean, String> checkResult = checkImplements(parentClass);

                    if (checkResult.getFirst()) {
                        final List<ClassReference> classExtends =
                                clazz.getExtendsList().getReferenceElements();

                        if (classExtends.isEmpty()) {
                            break;
                        }
                        if (problemsHolder instanceof UctProblemsHolder) {
                            ((UctProblemsHolder) problemsHolder).setReservedErrorCode(
                                    SupportedIssue.IMPLEMENTED_DEPRECATED_INTERFACE.getCode()
                            );
                        }
                        problemsHolder.registerProblem(
                                classExtends.get(0),
                                SupportedIssue.IMPLEMENTED_DEPRECATED_INTERFACE.getMessage(
                                        checkResult.getSecond()
                                ),
                                ProblemHighlightType.LIKE_DEPRECATED
                        );
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }

            /**
             * Check class implements.
             *
             * @param clazz PhpClass
             *
             * @return Pair[Boolean, String]
             */
            private Pair<Boolean, String> checkImplements(final PhpClass clazz) {
                if (clazz.isInterface()) {
                    return new Pair<>(false, null);
                }
                for (final ClassReference ref : clazz.getImplementsList().getReferenceElements()) {
                    final String interfaceFqn = ref.getFQN();
                    final PsiElement interfaceClass = ref.resolve();

                    if (interfaceFqn == null || !(interfaceClass instanceof PhpClass)) {
                        continue;
                    }

                    if (VersionStateManager.getInstance(clazz.getProject())
                            .isDeprecated(interfaceFqn)) {
                        return new Pair<>(true, interfaceFqn);
                    }
                    final Pair<Boolean, String> parentCheck = InheritedDeprecatedInterface
                            .hasDeprecatedParent((PhpClass) interfaceClass);

                    if (parentCheck.getFirst()) {
                        return parentCheck;
                    }
                }

                return new Pair<>(false, null);
            }
        };
    }
}
