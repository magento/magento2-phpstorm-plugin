/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.php;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeAnalyserVisitor;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public abstract class OverriddenFieldInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpTypeAnalyserVisitor() {

            @Override
            public void visitPhpField(final Field field) {
                final Project project = field.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);
                final PhpClass phpClass = field.getContainingClass();

                if (!settings.isEnabled()
                        || phpClass == null
                        || !isTypeValid(field)
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                super.visitPhpField(field);

                PhpClass parentClass = phpClass.getSuperClass();
                boolean isFound = false;

                while (parentClass != null && !isFound) {
                    for (final Field ownField : parentClass.getOwnFields()) {
                        if (!ownField.getModifier().isPrivate()
                                && isTypeValid(ownField)
                                && ownField.getName().equals(field.getName())
                        ) {
                            execute(project, problemsHolder, field, ownField, parentClass);
                            isFound = true;
                            break;
                        }
                    }
                    parentClass = parentClass.getSuperClass();
                }
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param field Field
     * @param overriddenField Field
     * @param parentClass PhpClass
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final Field field,
            final Field overriddenField,
            final PhpClass parentClass
    );

    /**
     * Implement this method to specify type check for the field.
     *
     * @param field Field
     *
     * @return boolean
     */
    protected abstract boolean isTypeValid(final Field field);

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
