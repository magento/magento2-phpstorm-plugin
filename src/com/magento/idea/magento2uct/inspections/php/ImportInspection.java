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
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import org.jetbrains.annotations.NotNull;

public abstract class ImportInspection extends PhpInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {

            @Override
            public void visitPhpUseList(final PhpUseList useList) {
                final Project project = useList.getProject();
                final UctSettingsService settings = UctSettingsService.getInstance(project);

                if (!settings.isEnabled()
                        || !settings.isIssueLevelSatisfiable(getSeverityLevel())) {
                    return;
                }
                final PsiFile file = useList.getContainingFile();
                final PhpClass phpClass = GetFirstClassOfFile.getInstance().execute((PhpFile) file);

                if (phpClass == null) {
                    return;
                }

                for (final PhpUse use : useList.getDeclarations()) {
                    if (use.getName().isEmpty()) {
                        continue;
                    }
                    execute(project, problemsHolder, use, isInterface(use));
                }
            }

            /**
             * Check if inspected use is an interface.
             *
             * @param use PhpUse
             *
             * @return boolean
             */
            private boolean isInterface(final PhpUse use) {
                final PhpReference phpReference = use.getTargetReference();

                if (phpReference != null) {
                    final PsiElement element = phpReference.resolve();

                    if (element == null) {
                        return use.getFQN().contains("Interface");
                    }

                    return element instanceof PhpClass && ((PhpClass) element).isInterface();
                }

                return use.getFQN().contains("Interface");
            }
        };
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param project Project
     * @param problemsHolder ProblemsHolder
     * @param use PhpUse
     * @param isInterface boolean
     */
    protected abstract void execute(
            final Project project,
            final @NotNull ProblemsHolder problemsHolder,
            final PhpUse use,
            final boolean isInterface
    );

    /**
     * Implement this method to specify issue severity level for target inspection.
     *
     * @return IssueSeverityLevel
     */
    protected abstract IssueSeverityLevel getSeverityLevel();
}
