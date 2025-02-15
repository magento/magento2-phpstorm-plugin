/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public abstract class InheritedInterfaceInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpClass(final PhpClass clazz) {
                final Project project = clazz.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!clazz.isInterface()
                        || !settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())
                ) {
                    return;
                }

                for (final ClassReference ref : clazz.getExtendsList().getReferenceElements()) {
                    final String interfaceFqn = ref.getFQN();
                    final PsiElement interfaceClass = ref.resolve();

                    if (interfaceFqn == null || !(interfaceClass instanceof PhpClass)) {
                        continue;
                    }
                    execute(project, problemsHolder, ref, interfaceFqn);
                }
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param reference ClassReference
     * @param interfaceFqn String
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassReference reference,
            final String interfaceFqn
    );

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
