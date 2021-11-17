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
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeAnalyserVisitor;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public abstract class UsedTypeInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpTypeAnalyserVisitor() {

            @Override
            public void visitPhpClassReference(final ClassReference reference) {
                final Project project = reference.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                final PsiElement resolved = reference.resolve();

                if (!(resolved instanceof PhpClass)) {
                    return;
                }
                final PhpClass phpClass = (PhpClass) resolved;

                execute(project, problemsHolder, phpClass, reference);
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param phpClass PhpClass
     * @param reference ClassReference
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpClass phpClass,
            final ClassReference reference
    );

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
