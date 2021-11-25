/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public abstract class UsedFieldInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpFieldReference(final FieldReference fieldReference) {
                final Project project = fieldReference.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                final PsiElement element = fieldReference.resolve();
                final PsiFile file = fieldReference.getContainingFile();

                if (!(element instanceof Field) || element.getContainingFile().equals(file)) {
                    return;
                }
                execute(project, problemsHolder, (Field) element, fieldReference);
            }

            @Override
            public void visitPhpClassConstantReference(
                    final ClassConstantReference constantReference
            ) {
                final Project project = constantReference.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                final PsiElement element = constantReference.resolve();
                final PsiFile file = constantReference.getContainingFile();

                if (!(element instanceof ClassConstImpl)
                        || element.getContainingFile().equals(file)) {
                    return;
                }
                execute(project, problemsHolder, (ClassConstImpl) element, constantReference);
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param field Field
     * @param fieldReference FieldReference
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final FieldReference fieldReference
    );

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param constant Field
     * @param constantReference FieldReference
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final ClassConstImpl constant,
            final ClassConstantReference constantReference
    );

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
